package com.example.projecthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.remote.supabase.models.TarefaDto
import com.example.projecthub.repository.ProjetoRepository
import com.example.projecthub.repository.TarefaRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class AdminTaskStatusFilter(val label: String) {
    All("Todas"),
    Pending("Pendentes"),
    Completed("Completadas"),
    Delayed("Atrasadas")
}

data class AdminTaskProjectOption(
    val id: Int,
    val name: String
)

data class AdminTaskListItem(
    val id: Int,
    val title: String,
    val description: String,
    val projectId: Int,
    val status: String,
    val statusLabel: String,
    val startDate: String,
    val dueDate: String
) {
    val isCompleted: Boolean
        get() = status.equals("CONCLUIDO", ignoreCase = true)

    val isDelayed: Boolean
        get() {
            val due = runCatching { LocalDate.parse(dueDate.take(10)) }.getOrNull()
            return !isCompleted && due != null && due.isBefore(LocalDate.now())
        }
}

data class AdminProjectTaskGroup(
    val projectId: Int,
    val projectName: String,
    val tasks: List<AdminTaskListItem>,
    val visibleTasks: List<AdminTaskListItem>,
    val isExpanded: Boolean = true
) {
    val totalTasks: Int
        get() = tasks.size

    val pendingTasks: Int
        get() = tasks.count { !it.isCompleted }

    val completedTasks: Int
        get() = tasks.count { it.isCompleted }
}

data class AdminTasksState(
    val projects: List<AdminTaskProjectOption> = emptyList(),
    val tasks: List<AdminTaskListItem> = emptyList(),
    val projectGroups: List<AdminProjectTaskGroup> = emptyList(),
    val expandedProjectIds: Set<Int> = emptySet(),
    val searchQuery: String = "",
    val selectedStatus: AdminTaskStatusFilter = AdminTaskStatusFilter.All,
    val totalTasks: Int = 0,
    val pendingTasks: Int = 0,
    val completedTasks: Int = 0,
    val isLoading: Boolean = true,
    val isCreating: Boolean = false,
    val errorMessage: String? = null,
    val createErrorMessage: String? = null
)

class AdminTasksViewModel(
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
    private val tarefaRepository: TarefaRepository = TarefaRepository()
) : ViewModel() {

    var state by mutableStateOf(AdminTasksState())
        private set

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
                .mapNotNull { project -> project.id?.let { AdminTaskProjectOption(it, project.nome) } }
                .sortedBy { it.name.lowercase() }

            val tasks = tasksResult.getOrDefault(emptyList())
                .mapNotNull { it.toListItem() }
                .sortedBy { it.title.lowercase() }

            state = state.copy(
                projects = projects,
                tasks = tasks,
                expandedProjectIds = projects.map { it.id }.toSet(),
                totalTasks = tasks.size,
                pendingTasks = tasks.count { !it.isCompleted },
                completedTasks = tasks.count { it.isCompleted },
                isLoading = false
            )
            applyFilters()
        }
    }

    fun updateSearchQuery(query: String) {
        state = state.copy(searchQuery = query)
        applyFilters()
    }

    fun updateStatusFilter(filter: AdminTaskStatusFilter) {
        state = state.copy(selectedStatus = filter)
        applyFilters()
    }

    fun toggleProject(projectId: Int) {
        val expanded = state.expandedProjectIds
        state = state.copy(
            expandedProjectIds = if (projectId in expanded) expanded - projectId else expanded + projectId
        )
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
            if (projectId == null) {
                state = state.copy(createErrorMessage = "Seleciona um projeto.")
                return@launch
            }

            state = state.copy(isCreating = true, createErrorMessage = null)
            val result = tarefaRepository.createTarefa(title, description, projectId, startDateText, endDateText)
            state = state.copy(isCreating = false)

            if (result.isSuccess) {
                onSuccess()
                loadTasks()
            } else {
                state = state.copy(createErrorMessage = result.exceptionOrNull()?.message ?: "Erro ao criar tarefa.")
            }
        }
    }

    private fun applyFilters() {
        val query = state.searchQuery.trim()
        val projectsById = state.projects.associateBy { it.id }

        val groups = state.projects.map { project ->
            val projectTasks = state.tasks.filter { it.projectId == project.id }
            val visibleTasks = projectTasks.filter { task ->
                val matchesQuery = query.isBlank() ||
                    task.title.contains(query, true) ||
                    task.description.contains(query, true) ||
                    project.name.contains(query, true)

                val matchesStatus = when (state.selectedStatus) {
                    AdminTaskStatusFilter.All -> true
                    AdminTaskStatusFilter.Pending -> !task.isCompleted
                    AdminTaskStatusFilter.Completed -> task.isCompleted
                    AdminTaskStatusFilter.Delayed -> task.isDelayed
                }

                matchesQuery && matchesStatus
            }

            AdminProjectTaskGroup(
                projectId = project.id,
                projectName = projectsById[project.id]?.name ?: project.name,
                tasks = projectTasks,
                visibleTasks = visibleTasks,
                isExpanded = project.id in state.expandedProjectIds
            )
        }.filter { group ->
            query.isBlank() && state.selectedStatus == AdminTaskStatusFilter.All || group.visibleTasks.isNotEmpty()
        }

        state = state.copy(projectGroups = groups)
    }

    private fun TarefaDto.toListItem(): AdminTaskListItem? {
        val taskId = id ?: return null

        return AdminTaskListItem(
            id = taskId,
            title = titulo,
            description = descricao?.takeIf { it.isNotBlank() } ?: "Sem descrição",
            projectId = projeto_id,
            status = status,
            statusLabel = status.replace("_", " "),
            startDate = data_inicio ?: "-",
            dueDate = data_fim ?: "-"
        )
    }
}
