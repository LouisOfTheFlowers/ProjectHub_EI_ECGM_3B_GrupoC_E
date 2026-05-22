package com.example.projecthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.remote.supabase.models.TarefaDto
import com.example.projecthub.repository.ObservacaoRepository
import com.example.projecthub.repository.ProjetoRepository
import com.example.projecthub.repository.ProjetoUserRepository
import com.example.projecthub.repository.RegistoTarefaRepository
import com.example.projecthub.repository.TarefaRepository
import com.example.projecthub.repository.TarefaUserRepository
import com.example.projecthub.remote.supabase.UserRemoteDataSource
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class GestorTaskStatusFilter(val label: String) {
    All("Todas"),
    Pending("Pendentes"),
    InProgress("Em progresso"),
    Completed("Concluidas")
}

data class GestorTaskProjectOption(
    val id: Int,
    val name: String
)

data class GestorTaskUserOption(
    val id: Int,
    val name: String,
    val projectIds: Set<Int>
)

data class GestorTaskAssignee(
    val id: Int,
    val name: String
)

data class GestorTaskListItem(
    val id: Int,
    val projectId: Int,
    val title: String,
    val description: String,
    val statusLabel: String,
    val rawStatus: String,
    val startDate: String,
    val dueDate: String,
    val assignees: List<GestorTaskAssignee>,
    val isCompleted: Boolean,
    val isInProgress: Boolean,
    val isPending: Boolean,
    val isDelayed: Boolean
)

data class GestorTaskInfoObservation(
    val id: Int?,
    val text: String,
    val userName: String,
    val date: String,
    val completionPercent: Int,
    val spentHours: Float?
)

data class GestorTaskInfoState(
    val task: GestorTaskListItem? = null,
    val observations: List<GestorTaskInfoObservation> = emptyList(),
    val recordsCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class GestorProjectTaskGroup(
    val projectId: Int,
    val projectName: String,
    val totalTasks: Int,
    val visibleTasks: List<GestorTaskListItem>,
    val completedTasks: Int,
    val inProgressTasks: Int,
    val pendingTasks: Int,
    val isExpanded: Boolean
)

data class GestorTasksState(
    val projectGroups: List<GestorProjectTaskGroup> = emptyList(),
    val projects: List<GestorTaskProjectOption> = emptyList(),
    val users: List<GestorTaskUserOption> = emptyList(),
    val expandedProjectIds: Set<Int> = emptySet(),
    val selectedStatus: GestorTaskStatusFilter = GestorTaskStatusFilter.All,
    val searchQuery: String = "",
    val totalTasks: Int = 0,
    val pendingTasks: Int = 0,
    val inProgressTasks: Int = 0,
    val completedTasks: Int = 0,
    val isLoading: Boolean = true,
    val isCreating: Boolean = false,
    val createErrorMessage: String? = null,
    val errorMessage: String? = null,
    val detailState: GestorTaskInfoState = GestorTaskInfoState()
)

class GestorTasksViewModel(
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
    private val tarefaRepository: TarefaRepository = TarefaRepository(),
    private val projetoUserRepository: ProjetoUserRepository = ProjetoUserRepository(),
    private val tarefaUserRepository: TarefaUserRepository = TarefaUserRepository(),
    private val registoTarefaRepository: RegistoTarefaRepository = RegistoTarefaRepository(),
    private val observacaoRepository: ObservacaoRepository = ObservacaoRepository(),
    private val userRemoteDataSource: UserRemoteDataSource = UserRemoteDataSource()
) : ViewModel() {

    private data class TaskGroupSource(
        val projectId: Int,
        val projectName: String,
        val tasks: List<GestorTaskListItem>
    )

    private var sourceGroups: List<TaskGroupSource> = emptyList()

    var state by mutableStateOf(GestorTasksState())
        private set

    fun loadTasks(gestorId: Int?) {
        if (gestorId == null) {
            state = GestorTasksState(
                isLoading = false,
                errorMessage = "Nao foi possivel identificar o gestor autenticado."
            )
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            val projectsResult = projetoRepository.getProjetosByGestor(gestorId)
            val tasksResult = tarefaRepository.getTarefas()
            val taskUsersResult = tarefaUserRepository.getTarefaUsers()
            val usersResult = runCatching { userRemoteDataSource.getUsers() }

            if (projectsResult.isFailure || tasksResult.isFailure || taskUsersResult.isFailure || usersResult.isFailure) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Nao foi possivel carregar as tarefas."
                )
                return@launch
            }

            val projects = projectsResult.getOrDefault(emptyList())
            val activeProjects = projects.filterNot { it.status.isCompletedStatus() }
            val projectIds = activeProjects.mapNotNull { it.id }.toSet()
            val projectUsers = projectIds.flatMap { projectId ->
                projetoUserRepository.getUsersByProjeto(projectId).getOrDefault(emptyList())
            }
            val usersById = usersResult.getOrDefault(emptyList())
                .mapNotNull { user -> user.id?.let { it to (user.nome.ifBlank { user.username }) } }
                .toMap()
            val userProjectIds = projectUsers.groupBy { it.user_id }.mapValues { entry ->
                entry.value.map { it.projeto_id }.toSet()
            }
            val userOptions = userProjectIds.mapNotNull { (userId, ids) ->
                usersById[userId]?.let { name -> GestorTaskUserOption(userId, name, ids) }
            }.sortedBy { it.name.lowercase() }

            val taskUsersByTask = taskUsersResult.getOrDefault(emptyList()).groupBy { it.tarefa_id }
            val tasksByProject = tasksResult.getOrDefault(emptyList())
                .filter { it.projeto_id in projectIds }
                .groupBy { it.projeto_id }

            sourceGroups = activeProjects.mapNotNull { project ->
                val projectId = project.id ?: return@mapNotNull null
                TaskGroupSource(
                    projectId = projectId,
                    projectName = project.nome,
                    tasks = tasksByProject[projectId]
                        .orEmpty()
                        .map { task ->
                            task.toListItem(
                                assignees = taskUsersByTask[task.id ?: -1]
                                    .orEmpty()
                                    .mapNotNull { relation ->
                                        usersById[relation.user_id]?.let {
                                            GestorTaskAssignee(relation.user_id, it)
                                        }
                                    }
                                    .sortedBy { it.name.lowercase() }
                            )
                        }
                        .sortedWith(compareBy<GestorTaskListItem> { it.isCompleted }.thenBy { it.title.lowercase() })
                )
            }.sortedBy { it.projectName.lowercase() }

            state = state.copy(
                projects = activeProjects.mapNotNull { project ->
                    project.id?.let { GestorTaskProjectOption(it, project.nome) }
                }.sortedBy { it.name.lowercase() },
                users = userOptions,
                expandedProjectIds = sourceGroups.map { it.projectId }.toSet(),
                isLoading = false
            )
            applyFilters()
        }
    }

    fun toggleProject(projectId: Int) {
        val expanded = state.expandedProjectIds
        state = state.copy(
            expandedProjectIds = if (projectId in expanded) expanded - projectId else expanded + projectId
        )
        applyFilters()
    }

    fun updateStatusFilter(filter: GestorTaskStatusFilter) {
        state = state.copy(selectedStatus = filter)
        applyFilters()
    }

    fun updateSearchQuery(query: String) {
        state = state.copy(searchQuery = query)
        applyFilters()
    }

    fun loadTaskInfo(task: GestorTaskListItem) {
        viewModelScope.launch {
            state = state.copy(
                detailState = GestorTaskInfoState(
                    task = task,
                    isLoading = true
                )
            )

            val recordsResult = registoTarefaRepository.getRegistosByTarefa(task.id)
            val observationsResult = observacaoRepository.getObservacoes()
            val usersResult = runCatching { userRemoteDataSource.getUsers() }

            if (recordsResult.isFailure || observationsResult.isFailure || usersResult.isFailure) {
                state = state.copy(
                    detailState = GestorTaskInfoState(
                        task = task,
                        isLoading = false,
                        errorMessage = "Nao foi possivel carregar os detalhes da tarefa."
                    )
                )
                return@launch
            }

            val usersById = usersResult.getOrDefault(emptyList())
                .mapNotNull { user -> user.id?.let { it to user } }
                .toMap()
            val records = recordsResult.getOrDefault(emptyList())
            val recordsById = records.mapNotNull { record -> record.id?.let { it to record } }.toMap()
            val observations = observationsResult
                .getOrDefault(emptyList())
                .mapNotNull { observation ->
                    val record = recordsById[observation.registo_id] ?: return@mapNotNull null
                    val user = usersById[record.user_id]
                    val userName = user?.nome?.ifBlank { user.username } ?: "Utilizador ${record.user_id}"

                    GestorTaskInfoObservation(
                        id = observation.id,
                        text = observation.texto,
                        userName = userName,
                        date = (observation.created_at ?: record.created_at ?: record.data).toUiDateText(),
                        completionPercent = record.taxa_conclusao,
                        spentHours = record.tempo_gasto
                    )
                }
                .sortedByDescending { it.date }

            state = state.copy(
                detailState = GestorTaskInfoState(
                    task = task,
                    observations = observations,
                    recordsCount = records.size,
                    isLoading = false
                )
            )
        }
    }

    fun clearTaskInfo() {
        state = state.copy(detailState = GestorTaskInfoState())
    }

    fun clearCreateError() {
        state = state.copy(createErrorMessage = null)
    }

    fun createTask(
        gestorId: Int?,
        title: String,
        description: String,
        projectId: Int?,
        startDateText: String,
        endDateText: String,
        userIds: Set<Int>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val startDate = startDateText.toInputLocalDateOrNull()
            val endDate = endDateText.toInputLocalDateOrNull()

            val validationError = when {
                title.isBlank() -> "Indica o titulo da tarefa."
                description.isBlank() -> "Indica a descricao da tarefa."
                projectId == null -> "Seleciona o projeto da tarefa."
                startDate == null -> "Indica a data de inicio no formato dd/mm/aaaa."
                endDate == null -> "Indica a data de fim no formato dd/mm/aaaa."
                startDate.isAfter(endDate) -> "A data de inicio nao pode ser depois da data de fim."
                userIds.isEmpty() -> "Associa pelo menos um utilizador a tarefa."
                else -> null
            }

            if (validationError != null || startDate == null || endDate == null || projectId == null) {
                state = state.copy(createErrorMessage = validationError)
                return@launch
            }

            state = state.copy(isCreating = true, createErrorMessage = null)

            val createResult = tarefaRepository.createTarefaReturning(
                titulo = title,
                descricao = description,
                projetoId = projectId,
                dataInicio = startDate.toString(),
                dataFim = endDate.toString()
            )

            val taskId = createResult.getOrNull()?.id
            if (createResult.isFailure || taskId == null) {
                state = state.copy(
                    isCreating = false,
                    createErrorMessage = createResult.exceptionOrNull()?.message
                        ?: "Nao foi possivel criar a tarefa."
                )
                return@launch
            }

            for (userId in userIds) {
                val associateResult = tarefaUserRepository.associarUserATarefa(taskId, userId)
                if (associateResult.isFailure) {
                    state = state.copy(
                        isCreating = false,
                        createErrorMessage = associateResult.exceptionOrNull()?.message
                            ?: "Nao foi possivel associar utilizadores a tarefa."
                    )
                    return@launch
                }
            }

            state = state.copy(isCreating = false)
            loadTasks(gestorId)
            onSuccess()
        }
    }

    fun deleteTask(gestorId: Int?, taskId: Int) {
        viewModelScope.launch {
            val result = tarefaRepository.deleteManagerTask(taskId)

            if (result.isSuccess) {
                loadTasks(gestorId)
            } else {
                state = state.copy(
                    errorMessage = result.exceptionOrNull()?.message
                        ?: "Nao foi possivel eliminar a tarefa."
                )
            }
        }
    }

    fun updateTask(
        gestorId: Int?,
        task: GestorTaskListItem,
        title: String,
        description: String,
        startDateText: String,
        endDateText: String,
        userIds: Set<Int>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val startDate = startDateText.toInputLocalDateOrNull()
            val endDate = endDateText.toInputLocalDateOrNull()

            val validationError = when {
                title.isBlank() -> "Indica o titulo da tarefa."
                description.isBlank() -> "Indica a descricao da tarefa."
                startDate == null -> "Indica a data de inicio no formato dd/mm/aaaa."
                endDate == null -> "Indica a data de fim no formato dd/mm/aaaa."
                startDate.isAfter(endDate) -> "A data de inicio nao pode ser depois da data de fim."
                userIds.isEmpty() -> "Associa pelo menos um utilizador a tarefa."
                else -> null
            }

            if (validationError != null || startDate == null || endDate == null) {
                state = state.copy(createErrorMessage = validationError)
                return@launch
            }

            state = state.copy(isCreating = true, createErrorMessage = null)

            val updateResult = tarefaRepository.updateTarefa(
                tarefaId = task.id,
                titulo = title,
                descricao = description,
                projetoId = task.projectId,
                status = task.rawStatus,
                dataInicio = startDate.toString(),
                dataFim = endDate.toString()
            )

            if (updateResult.isFailure) {
                state = state.copy(
                    isCreating = false,
                    createErrorMessage = updateResult.exceptionOrNull()?.message
                        ?: "Nao foi possivel atualizar a tarefa."
                )
                return@launch
            }

            val currentUserIds = task.assignees.map { it.id }.toSet()
            val usersToRemove = currentUserIds - userIds
            val usersToAdd = userIds - currentUserIds

            for (userId in usersToRemove) {
                val result = tarefaUserRepository.removerUserDaTarefa(task.id, userId)
                if (result.isFailure) {
                    state = state.copy(
                        isCreating = false,
                        createErrorMessage = result.exceptionOrNull()?.message
                            ?: "Nao foi possivel remover utilizadores da tarefa."
                    )
                    return@launch
                }
            }

            for (userId in usersToAdd) {
                val result = tarefaUserRepository.associarUserATarefa(task.id, userId)
                if (result.isFailure) {
                    state = state.copy(
                        isCreating = false,
                        createErrorMessage = result.exceptionOrNull()?.message
                            ?: "Nao foi possivel associar utilizadores a tarefa."
                    )
                    return@launch
                }
            }

            state = state.copy(isCreating = false)
            loadTasks(gestorId)
            onSuccess()
        }
    }

    private fun applyFilters() {
        val query = state.searchQuery.trim()
        val groups = sourceGroups.map { group ->
            val visibleTasks = group.tasks.filter { task ->
                val matchesStatus = when (state.selectedStatus) {
                    GestorTaskStatusFilter.All -> true
                    GestorTaskStatusFilter.Pending -> task.isPending
                    GestorTaskStatusFilter.InProgress -> task.isInProgress
                    GestorTaskStatusFilter.Completed -> task.isCompleted
                }

                val matchesSearch = query.isBlank() ||
                    group.projectName.contains(query, ignoreCase = true) ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true) ||
                    task.assignees.any { it.name.contains(query, ignoreCase = true) }

                matchesStatus && matchesSearch
            }

            GestorProjectTaskGroup(
                projectId = group.projectId,
                projectName = group.projectName,
                totalTasks = group.tasks.size,
                visibleTasks = visibleTasks,
                completedTasks = group.tasks.count { it.isCompleted },
                inProgressTasks = group.tasks.count { it.isInProgress },
                pendingTasks = group.tasks.count { it.isPending },
                isExpanded = group.projectId in state.expandedProjectIds
            )
        }.filter { group ->
            query.isBlank() && state.selectedStatus == GestorTaskStatusFilter.All || group.visibleTasks.isNotEmpty()
        }

        val allTasks = sourceGroups.flatMap { it.tasks }
        state = state.copy(
            projectGroups = groups,
            totalTasks = allTasks.size,
            completedTasks = allTasks.count { it.isCompleted },
            inProgressTasks = allTasks.count { it.isInProgress },
            pendingTasks = allTasks.count { it.isPending }
        )
    }

    private fun TarefaDto.toListItem(assignees: List<GestorTaskAssignee>): GestorTaskListItem {
        val dueDate = data_fim?.toLocalDateOrNull()
        val startDate = data_inicio?.take(10)?.toLocalDateOrNull()
        val isCompleted = status.isCompletedStatus()
        val isPending = !isCompleted && startDate?.isAfter(LocalDate.now()) == true
        val isInProgress = !isCompleted && !isPending && (
            status.isInProgressStatus() ||
                status.isPendingStatus() ||
                startDate?.isEqual(LocalDate.now()) == true ||
                startDate?.isBefore(LocalDate.now()) == true
            )
        val isDelayed = !isCompleted && dueDate != null && dueDate.isBefore(LocalDate.now())

        return GestorTaskListItem(
            id = id ?: 0,
            projectId = projeto_id,
            title = titulo,
            description = descricao?.takeIf { it.isNotBlank() } ?: "Sem descricao",
            statusLabel = when {
                isCompleted -> "Concluida"
                isDelayed -> "Atrasada"
                isInProgress -> "Em progresso"
                else -> "Pendente"
            },
            rawStatus = status,
            startDate = data_inicio?.take(10) ?: "-",
            dueDate = data_fim?.take(10) ?: "-",
            assignees = assignees,
            isCompleted = isCompleted,
            isInProgress = isInProgress,
            isPending = isPending,
            isDelayed = isDelayed
        )
    }

    private fun String.isCompletedStatus(): Boolean {
        return normalizedStatus() in setOf("CONCLUIDO", "CONCLUIDA", "COMPLETO", "COMPLETA", "COMPLETADO", "COMPLETADA", "FINALIZADO", "FINALIZADA")
    }

    private fun String.isInProgressStatus(): Boolean {
        return normalizedStatus() in setOf("EM_PROGRESSO", "EMPROGRESSO", "EM_ANDAMENTO", "ANDAMENTO", "A_DECORRER", "IN_PROGRESS", "INPROGRESS")
    }

    private fun String.isPendingStatus(): Boolean {
        return normalizedStatus() in setOf("PENDENTE", "POR_INICIAR", "NAO_INICIADO")
    }

    private fun String.normalizedStatus(): String {
        val withoutAccents = Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")

        return withoutAccents.trim().replace(" ", "_").replace("-", "_").uppercase()
    }

    private fun String.toLocalDateOrNull(): LocalDate? {
        return runCatching { LocalDate.parse(take(10)) }.getOrNull()
    }

    private fun String.toInputLocalDateOrNull(): LocalDate? {
        val trimmed = trim()
        return runCatching {
            LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }.getOrElse {
            runCatching { LocalDate.parse(trimmed) }.getOrNull()
        }
    }

    private fun String?.toUiDateText(): String {
        val date = this?.take(10)?.toLocalDateOrNull() ?: return "-"
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
}
