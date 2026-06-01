package com.example.projecthub.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.remote.supabase.models.TarefaDto
import com.example.projecthub.repository.ProjetoRepository
import com.example.projecthub.repository.TarefaRepository
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.LocalDate

enum class AdminTaskStatusFilter(val label: String) {
    All("Todas"),
    Pending("Pendentes"),
    Completed("Concluídas")
}

data class AdminTaskProjectOption(
    val id: Int,
    val name: String
)

data class AdminTaskListItem(
    override val id: Int,
    override val title: String,
    override val description: String,
    override val statusLabel: String,
    override val startDate: String,
    override val dueDate: String,
    override val isCompleted: Boolean,
    override val isDelayed: Boolean
) : TaskUiListItem

data class AdminProjectTaskGroup(
    val projectId: Int,
    val projectName: String,
    val totalTasks: Int,
    val visibleTasks: List<AdminTaskListItem>,
    val completedTasks: Int,
    val pendingTasks: Int,
    val isExpanded: Boolean
)

data class AdminTasksState(
    val projectGroups: List<AdminProjectTaskGroup> = emptyList(),
    val projects: List<AdminTaskProjectOption> = emptyList(),
    val expandedProjectIds: Set<Int> = emptySet(),
    val selectedStatus: AdminTaskStatusFilter = AdminTaskStatusFilter.All,
    val searchQuery: String = "",
    val totalTasks: Int = 0,
    val pendingTasks: Int = 0,
    val completedTasks: Int = 0,
    val isLoading: Boolean = true,
    val isCreating: Boolean = false,
    val createErrorMessage: String? = null,
    val errorMessage: String? = null
)

class AdminTasksViewModel(
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
    private val tarefaRepository: TarefaRepository = TarefaRepository()
) : ViewModel() {

    private data class TaskGroupSource(
        val projectId: Int,
        val projectName: String,
        val tasks: List<AdminTaskListItem>
    )

    private var sourceGroups: List<TaskGroupSource> = emptyList()

    private val _state = MutableStateFlow(AdminTasksState())
    val stateFlow: StateFlow<AdminTasksState> = _state
    private var state: AdminTasksState
        get() = _state.value
        set(value) { _state.value = value }

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            val projectsResult = projetoRepository.getProjetos()
            val tasksResult = tarefaRepository.getTarefas()

            if (projectsResult.isFailure || tasksResult.isFailure) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar as tarefas."
                )
                return@launch
            }

            val projects = projectsResult.getOrDefault(emptyList())
            val projectOptions = projects
                .mapNotNull { project ->
                    project.id?.let { id ->
                        AdminTaskProjectOption(
                            id = id,
                            name = project.nome
                        )
                    }
                }
                .sortedBy { it.name }
            val tasksByProject = tasksResult.getOrDefault(emptyList())
                .groupBy { it.projeto_id }

            val projectGroups = projects
                .mapNotNull { project ->
                    val projectId = project.id ?: return@mapNotNull null
                    TaskGroupSource(
                        projectId = projectId,
                        projectName = project.nome,
                        tasks = tasksByProject[projectId]
                            .orEmpty()
                            .map { it.toListItem() }
                            .sortedWith(compareBy<AdminTaskListItem> { it.isCompleted }.thenBy { it.title })
                    )
                }
                .sortedBy { it.projectName }

            val knownProjectIds = projectGroups.map { it.projectId }.toSet()
            val orphanTasks = tasksByProject
                .filterKeys { it !in knownProjectIds }
                .flatMap { it.value }
                .map { it.toListItem() }

            sourceGroups = if (orphanTasks.isEmpty()) {
                projectGroups
            } else {
                projectGroups + TaskGroupSource(
                    projectId = -1,
                    projectName = "Sem projeto",
                    tasks = orphanTasks.sortedWith(compareBy<AdminTaskListItem> { it.isCompleted }.thenBy { it.title })
                )
            }

            state = state.copy(
                projects = projectOptions,
                expandedProjectIds = sourceGroups.map { it.projectId }.toSet(),
                isLoading = false
            )
            applyFilters()
        }
    }

    fun toggleProject(projectId: Int) {
        val expanded = state.expandedProjectIds
        state = state.copy(
            expandedProjectIds = if (projectId in expanded) {
                expanded - projectId
            } else {
                expanded + projectId
            }
        )
        applyFilters()
    }

    fun updateStatusFilter(filter: AdminTaskStatusFilter) {
        state = state.copy(selectedStatus = filter)
        applyFilters()
    }

    fun updateSearchQuery(query: String) {
        state = state.copy(searchQuery = query)
        applyFilters()
    }

    fun clearCreateError() {
        state = state.copy(createErrorMessage = null)
    }

    fun createTask(
        title: String,
        description: String,
        projectId: Int?,
        startDateText: String,
        endDateText: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val startDate = startDateText.toInputLocalDateOrNull()
            val endDate = endDateText.toInputLocalDateOrNull()

            val validationError = when {
                title.isBlank() -> "Indica o título da tarefa."
                description.isBlank() -> "Indica a descrição da tarefa."
                projectId == null -> "Seleciona o projeto da tarefa."
                startDate == null -> "Indica a data de início no formato dd/mm/aaaa."
                endDate == null -> "Indica a data de fim no formato dd/mm/aaaa."
                startDate.isAfter(endDate) -> "A data de início não pode ser depois da data de fim."
                else -> null
            }

            if (validationError != null || startDate == null || endDate == null || projectId == null) {
                state = state.copy(createErrorMessage = validationError)
                return@launch
            }

            state = state.copy(isCreating = true, createErrorMessage = null)

            val result = tarefaRepository.createTarefa(
                titulo = title,
                descricao = description,
                projetoId = projectId,
                dataInicio = startDate.toString(),
                dataFim = endDate.toString()
            )

            if (result.isSuccess) {
                state = state.copy(isCreating = false)
                loadTasks()
                onSuccess()
            } else {
                state = state.copy(
                    isCreating = false,
                    createErrorMessage = taskErrorMessage(
                        result.exceptionOrNull(),
                        fallback = "Não foi possível criar a tarefa."
                    )
                )
            }
        }
    }

    private fun applyFilters() {
        val query = state.searchQuery.trim()
        val groups = sourceGroups.map { group ->
            val visibleTasks = group.tasks.filter { task ->
                val matchesStatus = when (state.selectedStatus) {
                    AdminTaskStatusFilter.All -> true
                    AdminTaskStatusFilter.Pending -> !task.isCompleted
                    AdminTaskStatusFilter.Completed -> task.isCompleted
                }

                val matchesSearch = query.isBlank() ||
                    group.projectName.contains(query, ignoreCase = true) ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)

                matchesStatus && matchesSearch
            }

            AdminProjectTaskGroup(
                projectId = group.projectId,
                projectName = group.projectName,
                totalTasks = group.tasks.size,
                visibleTasks = visibleTasks,
                completedTasks = group.tasks.count { it.isCompleted },
                pendingTasks = group.tasks.count { !it.isCompleted },
                isExpanded = group.projectId in state.expandedProjectIds
            )
        }.filter { group ->
            query.isBlank() && state.selectedStatus == AdminTaskStatusFilter.All || group.visibleTasks.isNotEmpty()
        }

        val allTasks = sourceGroups.flatMap { it.tasks }
        state = state.copy(
            projectGroups = groups,
            totalTasks = allTasks.size,
            completedTasks = allTasks.count { it.isCompleted },
            pendingTasks = allTasks.count { !it.isCompleted }
        )
    }

    private fun TarefaDto.toListItem(): AdminTaskListItem {
        val dueDate = data_fim?.toLocalDateOrNull()
        val isCompleted = status.isCompletedStatus()
        val isDelayed = !isCompleted && dueDate != null && dueDate.isBefore(LocalDate.now())

        return AdminTaskListItem(
            id = id ?: 0,
            title = titulo,
            description = descricao?.takeIf { it.isNotBlank() } ?: "Sem descrição",
            statusLabel = when {
                isCompleted -> "Completada"
                isDelayed -> "Atrasada"
                else -> "Pendente"
            },
            startDate = data_inicio?.take(10) ?: "-",
            dueDate = data_fim?.take(10) ?: "-",
            isCompleted = isCompleted,
            isDelayed = isDelayed
        )
    }

    private fun String.isCompletedStatus(): Boolean {
        return normalizedStatus() in setOf(
            "CONCLUIDO",
            "CONCLUIDA",
            "COMPLETO",
            "COMPLETA",
            "COMPLETADO",
            "COMPLETADA",
            "FINALIZADO",
            "FINALIZADA"
        )
    }

    private fun String.normalizedStatus(): String {
        val withoutAccents = Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")

        return withoutAccents
            .trim()
            .replace(" ", "_")
            .replace("-", "_")
            .uppercase()
    }

    private fun String.toLocalDateOrNull(): LocalDate? {
        return runCatching { LocalDate.parse(take(10)) }.getOrNull()
    }

    private fun String.toInputLocalDateOrNull(): LocalDate? {
        val trimmed = trim()

        return runCatching {
            LocalDate.parse(trimmed, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }.getOrElse {
            runCatching { LocalDate.parse(trimmed) }.getOrNull()
        }
    }

    private fun taskErrorMessage(error: Throwable?, fallback: String): String {
        val rawMessage = error?.message.orEmpty()

        return when {
            rawMessage.contains("row-level security", ignoreCase = true) ||
                rawMessage.contains("42501", ignoreCase = true) ->
                "Sem permissão para criar tarefas. Aplica a migration de policies das tarefas no Supabase e confirma que a tua conta tem role ADMIN."

            rawMessage.contains("network", ignoreCase = true) ||
                rawMessage.contains("timeout", ignoreCase = true) ->
                "Não foi possível ligar ao Supabase. Verifica a ligação à internet."

            rawMessage.isBlank() -> fallback

            else -> rawMessage
                .lineSequence()
                .firstOrNull()
                ?.take(140)
                ?: fallback
        }
    }
}
