package com.example.projecthub.viewmodel.admin

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.remote.supabase.UserRemoteDataSource
import com.example.projecthub.remote.supabase.models.ProjetoDto
import com.example.projecthub.remote.supabase.models.TarefaDto
import com.example.projecthub.repository.AvaliacaoRepository
import com.example.projecthub.repository.ObservacaoFotoRepository
import com.example.projecthub.repository.ObservacaoRepository
import com.example.projecthub.repository.ProjetoRepository
import com.example.projecthub.repository.ProjetoUserRepository
import com.example.projecthub.repository.RegistoTarefaRepository
import com.example.projecthub.repository.TarefaRepository
import com.example.projecthub.repository.TarefaUserRepository
import com.example.projecthub.viewmodel.ProjectUiListItem
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class AdminProjectManager(
    val id: Int,
    val name: String
)

data class AdminProjectListItem(
    override val id: Int,
    override val name: String,
    override val description: String,
    val coordinator: String,
    val managerId: Int?,
    override val statusLabel: String,
    override val startDate: String,
    override val dueDate: String,
    val memberCount: Int,
    val isCompleted: Boolean,
    val isDelayed: Boolean,
    val isInProgress: Boolean,
    val isExpanded: Boolean = false
) : ProjectUiListItem

data class AdminProjectInfoParticipant(
    val id: Int,
    val name: String,
    val email: String,
    val rating: Int?,
    val comment: String?
)

data class AdminProjectInfoObservation(
    val id: Int?,
    val text: String,
    val userName: String,
    val date: String,
    val local: String,
    val completionPercent: Int,
    val spentHours: Float?,
    val photoUrls: List<String>
)

data class AdminProjectInfoTask(
    val id: Int,
    val title: String,
    val description: String,
    val statusLabel: String,
    val startDate: String,
    val dueDate: String,
    val assignees: List<String>,
    val observations: List<AdminProjectInfoObservation>
)

data class AdminProjectInfoState(
    val project: AdminProjectListItem? = null,
    val participants: List<AdminProjectInfoParticipant> = emptyList(),
    val tasks: List<AdminProjectInfoTask> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class AdminProjectsState(
    val projects: List<AdminProjectListItem> = emptyList(),
    val visibleProjects: List<AdminProjectListItem> = emptyList(),
    val managers: List<AdminProjectManager> = emptyList(),
    val coordinators: List<String> = emptyList(),
    val completedCount: Int = 0,
    val inProgressCount: Int = 0,
    val delayedCount: Int = 0,
    val searchQuery: String = "",
    val selectedStatus: String = "Todos",
    val selectedCoordinator: String = "Todos",
    val isLoading: Boolean = true,
    val isCreating: Boolean = false,
    val createErrorMessage: String? = null,
    val errorMessage: String? = null,
    val detailState: AdminProjectInfoState = AdminProjectInfoState()
)

class AdminProjectsViewModel(
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
    private val projetoUserRepository: ProjetoUserRepository = ProjetoUserRepository(),
    private val tarefaRepository: TarefaRepository = TarefaRepository(),
    private val tarefaUserRepository: TarefaUserRepository = TarefaUserRepository(),
    private val registoTarefaRepository: RegistoTarefaRepository = RegistoTarefaRepository(),
    private val observacaoRepository: ObservacaoRepository = ObservacaoRepository(),
    private val observacaoFotoRepository: ObservacaoFotoRepository = ObservacaoFotoRepository(),
    private val avaliacaoRepository: AvaliacaoRepository = AvaliacaoRepository(),
    private val userRemoteDataSource: UserRemoteDataSource = UserRemoteDataSource()
) : ViewModel() {

    private val _state = MutableStateFlow(AdminProjectsState())
    val stateFlow: StateFlow<AdminProjectsState> = _state
    private var state: AdminProjectsState
        get() = _state.value
        set(value) { _state.value = value }

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            val projectsResult = projetoRepository.getProjetos()
            val projectUsersResult = projetoUserRepository.getProjetoUsers()
            val projectMemberCountsResult = projetoUserRepository.getProjectMemberCounts()
            val tasksResult = tarefaRepository.getTarefas()
            val taskUsersResult = tarefaUserRepository.getTarefaUsers()
            val usersResult = runCatching { userRemoteDataSource.getUsers() }

            if (
                projectsResult.isFailure ||
                projectUsersResult.isFailure ||
                tasksResult.isFailure ||
                taskUsersResult.isFailure ||
                usersResult.isFailure
            ) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar os projetos."
                )
                return@launch
            }

            val users = usersResult.getOrDefault(emptyList())
            val usersById = users
                .mapNotNull { user -> user.id?.let { it to user.nome } }
                .toMap()
            val managers = users
                .filter { it.role.normalizedStatus() in setOf("GESTOR") }
                .ifEmpty {
                    users.filterNot { it.role.normalizedStatus() == "ADMIN" }
                }
                .mapNotNull { user ->
                    user.id?.let {
                        AdminProjectManager(
                            id = it,
                            name = user.nome.ifBlank { user.username }
                        )
                    }
                }
                .sortedBy { it.name }

            val directProjectMemberCounts = projectUsersResult.getOrDefault(emptyList())
                .groupingBy { it.projeto_id }
                .eachCount()
            val taskProjectById = tasksResult
                .getOrDefault(emptyList())
                .mapNotNull { task -> task.id?.let { taskId -> taskId to task.projeto_id } }
                .toMap()
            val projectMemberIds = mutableMapOf<Int, MutableSet<Int>>()
            projectUsersResult.getOrDefault(emptyList()).forEach { relation ->
                projectMemberIds.getOrPut(relation.projeto_id) { mutableSetOf() }.add(relation.user_id)
            }
            taskUsersResult.getOrDefault(emptyList()).forEach { relation ->
                val projectId = taskProjectById[relation.tarefa_id] ?: return@forEach
                projectMemberIds.getOrPut(projectId) { mutableSetOf() }.add(relation.user_id)
            }
            val projectMemberCounts = projectMemberCountsResult
                .getOrDefault(emptyMap())
                .toMutableMap()
                .apply {
                    directProjectMemberCounts.forEach { (projectId, count) ->
                        put(projectId, maxOf(this[projectId] ?: 0, count))
                    }
                    projectMemberIds.forEach { (projectId, userIds) ->
                        put(projectId, maxOf(this[projectId] ?: 0, userIds.size))
                    }
                }
            val oldExpandedIds = state.projects
                .filter { it.isExpanded }
                .map { it.id }
                .toSet()

            val projects = projectsResult.getOrDefault(emptyList())
                .mapNotNull { projeto -> projeto.toListItem(usersById, projectMemberCounts) }
                .map { project -> project.copy(isExpanded = project.id in oldExpandedIds) }
                .sortedBy { it.name }

            state = state.copy(
                projects = projects,
                managers = managers,
                coordinators = projects.map { it.coordinator }
                    .filter { it != "Sem coordenador" }
                    .distinct()
                    .sorted(),
                completedCount = projects.count { it.isCompleted },
                delayedCount = projects.count { it.isDelayed },
                inProgressCount = projects.count { it.isInProgress && !it.isDelayed },
                isLoading = false,
                errorMessage = null
            )

            applyFilters()
        }
    }

    fun updateSearchQuery(query: String) {
        state = state.copy(searchQuery = query)
        applyFilters()
    }

    fun updateStatusFilter(status: String) {
        state = state.copy(selectedStatus = status)
        applyFilters()
    }

    fun updateCoordinatorFilter(coordinator: String) {
        state = state.copy(selectedCoordinator = coordinator)
        applyFilters()
    }

    fun toggleProject(projectId: Int) {
        state = state.copy(
            projects = state.projects.map { project ->
                if (project.id == projectId) project.copy(isExpanded = !project.isExpanded) else project
            }
        )
        applyFilters()
    }

    fun loadProjectInfo(project: AdminProjectListItem) {
        viewModelScope.launch {
            state = state.copy(
                detailState = AdminProjectInfoState(
                    project = project,
                    isLoading = true
                )
            )

            val tasksResult = tarefaRepository.getTarefasByProjeto(project.id)
            val projectUsersResult = projetoUserRepository.getUsersByProjeto(project.id)
            val taskUsersResult = tarefaUserRepository.getTarefaUsers()
            val ratingsResult = avaliacaoRepository.getAvaliacoesByProjeto(project.id)
            val allRatingsResult = avaliacaoRepository.getAvaliacoes()
            val recordsResult = registoTarefaRepository.getRegistos()
            val observationsResult = observacaoRepository.getObservacoes()
            val photosResult = observacaoFotoRepository.getFotos()
            val usersResult = runCatching { userRemoteDataSource.getUsers() }

            if (
                tasksResult.isFailure ||
                projectUsersResult.isFailure ||
                taskUsersResult.isFailure ||
                ratingsResult.isFailure ||
                allRatingsResult.isFailure ||
                recordsResult.isFailure ||
                observationsResult.isFailure ||
                photosResult.isFailure ||
                usersResult.isFailure
            ) {
                state = state.copy(
                    detailState = AdminProjectInfoState(
                        project = project,
                        isLoading = false,
                        errorMessage = "Não foi possível carregar os detalhes do projeto."
                    )
                )
                return@launch
            }

            val usersById = usersResult
                .getOrDefault(emptyList())
                .mapNotNull { user -> user.id?.let { id -> id to user } }
                .toMap()

            val ratingsByUser = (
                ratingsResult.getOrDefault(emptyList()) +
                    allRatingsResult
                        .getOrDefault(emptyList())
                        .filter { it.projeto_id == project.id }
                )
                .distinctBy { it.user_id }
                .associateBy { it.user_id }

            val tasks = tasksResult
                .getOrDefault(emptyList())
                .sortedBy { it.titulo.lowercase() }

            val taskIds = tasks.mapNotNull { it.id }.toSet()
            val taskUsersByTask = taskUsersResult
                .getOrDefault(emptyList())
                .filter { it.tarefa_id in taskIds }
                .groupBy { it.tarefa_id }

            val participantUserIds = (
                projectUsersResult.getOrDefault(emptyList()).map { it.user_id } +
                    taskUsersByTask.values.flatten().map { it.user_id }
                )
                .toSet()

            val participants = participantUserIds
                .mapNotNull { userId ->
                    val user = usersById[userId] ?: return@mapNotNull null
                    val rating = ratingsByUser[userId]

                    AdminProjectInfoParticipant(
                        id = userId,
                        name = user.nome.ifBlank { user.username },
                        email = user.email,
                        rating = rating?.classificacao,
                        comment = rating?.comentario
                    )
                }
                .sortedBy { it.name.lowercase() }

            val records = recordsResult
                .getOrDefault(emptyList())
                .filter { it.tarefa_id in taskIds }

            val recordsById = records
                .mapNotNull { record -> record.id?.let { id -> id to record } }
                .toMap()

            val photosByObservation = photosResult
                .getOrDefault(emptyList())
                .groupBy { it.observacao_id }

            val observationsByTask = observationsResult
                .getOrDefault(emptyList())
                .mapNotNull { observation ->
                    val record = recordsById[observation.registo_id] ?: return@mapNotNull null
                    val user = usersById[record.user_id]
                    val userName = user?.nome?.ifBlank { user.username }
                        ?: "Utilizador ${record.user_id}"

                    record.tarefa_id to AdminProjectInfoObservation(
                        id = observation.id,
                        text = observation.texto,
                        userName = userName,
                        date = (observation.created_at ?: record.created_at ?: record.data).toUiDateText(),
                        local = record.local?.takeIf { it.isNotBlank() } ?: "-",
                        completionPercent = record.taxa_conclusao,
                        spentHours = record.tempo_gasto,
                        photoUrls = observation.id
                            ?.let { observationId ->
                                photosByObservation[observationId]
                                    .orEmpty()
                                    .map { photo -> photo.foto_url }
                            }
                            .orEmpty()
                    )
                }
                .groupBy(
                    keySelector = { it.first },
                    valueTransform = { it.second }
                )

            val infoTasks = tasks.mapNotNull { task ->
                val taskId = task.id ?: return@mapNotNull null
                val assignees = taskUsersByTask[taskId]
                    .orEmpty()
                    .mapNotNull { relation ->
                        usersById[relation.user_id]?.let { user ->
                            user.nome.ifBlank { user.username }
                        }
                    }
                    .sortedBy { it.lowercase() }

                AdminProjectInfoTask(
                    id = taskId,
                    title = task.titulo,
                    description = task.descricao?.takeIf { it.isNotBlank() } ?: "Sem descrição",
                    statusLabel = task.toTaskStatusLabel(),
                    startDate = task.data_inicio.toUiDateText(),
                    dueDate = task.data_fim.toUiDateText(),
                    assignees = assignees,
                    observations = observationsByTask[taskId]
                        .orEmpty()
                        .sortedByDescending { it.date }
                )
            }

            state = state.copy(
                detailState = AdminProjectInfoState(
                    project = project,
                    participants = participants,
                    tasks = infoTasks,
                    isLoading = false
                )
            )
        }
    }

    fun clearProjectInfo() {
        state = state.copy(detailState = AdminProjectInfoState())
    }

    fun clearCreateError() {
        state = state.copy(createErrorMessage = null)
    }

    fun createProject(
        name: String,
        description: String,
        startDateText: String,
        endDateText: String,
        managerId: Int?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val startDate = startDateText.parseProjectDateOrNull()
            val endDate = endDateText.parseProjectDateOrNull()

            val validationError = when {
                name.isBlank() -> "Indica o nome do projeto."
                description.isBlank() -> "Indica a descrição do projeto."
                startDate == null -> "Indica a data de início no formato dd/mm/aaaa."
                endDate == null -> "Indica a data de fim no formato dd/mm/aaaa."
                startDate.isBefore(today) -> "A data de início não pode ser anterior a hoje."
                endDate.isBefore(today) -> "A data de fim não pode ser anterior a hoje."
                startDate.isAfter(endDate) -> "A data de início não pode ser depois da data de fim."
                managerId == null -> "Seleciona um gestor para o projeto."
                else -> null
            }

            if (validationError != null || startDate == null || endDate == null) {
                state = state.copy(createErrorMessage = validationError)
                return@launch
            }

            val status = if (startDate == today) "EM_PROGRESSO" else "PENDENTE"

            state = state.copy(isCreating = true, createErrorMessage = null)

            val result = projetoRepository.createProjeto(
                nome = name,
                descricao = description,
                dataInicio = startDate.toString(),
                dataFim = endDate.toString(),
                gestorId = managerId,
                status = status
            )

            if (result.isSuccess) {
                state = state.copy(isCreating = false)
                loadProjects()
                onSuccess()
            } else {
                state = state.copy(
                    isCreating = false,
                    createErrorMessage = result.exceptionOrNull()?.message
                        ?: "Não foi possível criar o projeto."
                )
            }
        }
    }

    fun deleteProject(projectId: Int) {
        viewModelScope.launch {
            val result = projetoRepository.deleteProjeto(projectId)

            if (result.isSuccess) {
                loadProjects()
            } else {
                state = state.copy(
                    errorMessage = result.exceptionOrNull()?.message
                        ?: "Não foi possível apagar o projeto."
                )
            }
        }
    }

    fun updateProject(
        projectId: Int,
        name: String,
        description: String,
        startDateText: String,
        endDateText: String,
        managerId: Int?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val startDate = startDateText.parseProjectDateOrNull()
            val endDate = endDateText.parseProjectDateOrNull()

            val validationError = when {
                name.isBlank() -> "Indica o nome do projeto."
                description.isBlank() -> "Indica a descrição do projeto."
                startDate == null -> "Indica a data de início no formato dd/mm/aaaa."
                endDate == null -> "Indica a data de fim no formato dd/mm/aaaa."
                startDate.isAfter(endDate) -> "A data de início não pode ser depois da data de fim."
                managerId == null -> "Seleciona um gestor para o projeto."
                else -> null
            }

            if (validationError != null || startDate == null || endDate == null) {
                state = state.copy(createErrorMessage = validationError)
                return@launch
            }

            val status = if (startDate.isAfter(today)) "PENDENTE" else "EM_PROGRESSO"

            state = state.copy(isCreating = true, createErrorMessage = null)

            val result = projetoRepository.updateProjeto(
                projetoId = projectId,
                nome = name,
                descricao = description,
                dataInicio = startDate.toString(),
                dataFim = endDate.toString(),
                status = status,
                gestorId = managerId
            )

            if (result.isSuccess) {
                state = state.copy(isCreating = false)
                loadProjects()
                onSuccess()
            } else {
                state = state.copy(
                    isCreating = false,
                    createErrorMessage = result.exceptionOrNull()?.message
                        ?: "Não foi possível atualizar o projeto."
                )
            }
        }
    }

    private fun applyFilters() {
        val query = state.searchQuery.trim()
        val visibleProjects = state.projects.filter { project ->
            val matchesSearch = query.isBlank() ||
                project.name.contains(query, ignoreCase = true) ||
                project.description.contains(query, ignoreCase = true) ||
                project.coordinator.contains(query, ignoreCase = true)

            val matchesStatus = when (state.selectedStatus) {
                "Concluídos" -> project.isCompleted
                "Em progresso" -> project.isInProgress && !project.isDelayed
                "Atrasados" -> project.isDelayed
                else -> true
            }

            val matchesCoordinator = state.selectedCoordinator == "Todos" ||
                project.coordinator == state.selectedCoordinator

            matchesSearch && matchesStatus && matchesCoordinator
        }

        state = state.copy(visibleProjects = visibleProjects)
    }

    private fun ProjetoDto.toListItem(
        usersById: Map<Int, String>,
        memberCounts: Map<Int, Int>
    ): AdminProjectListItem? {
        val projectId = id ?: return null
        val today = LocalDate.now()
        val startDate = data_inicio?.toLocalDateOrNull()
        val dueDate = data_fim?.toLocalDateOrNull()
        val isCompleted = status.isCompletedStatus()
        val isDelayed = !isCompleted && dueDate != null && dueDate.isBefore(today)
        val hasStarted = startDate == null || !startDate.isAfter(today)
        val isInProgress = !isCompleted && !isDelayed && hasStarted && (
            status.isInProgressStatus() ||
                status.isPendingStatus()
        )

        return AdminProjectListItem(
            id = projectId,
            name = nome,
            description = descricao?.takeIf { it.isNotBlank() } ?: "Sem descrição",
            coordinator = gestor_id?.let { usersById[it] } ?: "Sem coordenador",
            managerId = gestor_id,
            statusLabel = when {
                isCompleted -> "Concluído"
                isDelayed -> "Atrasado"
                isInProgress -> "Em progresso"
                else -> "Pendente"
            },
            startDate = data_inicio?.take(10) ?: "-",
            dueDate = data_fim?.take(10) ?: "-",
            memberCount = memberCounts[projectId] ?: 0,
            isCompleted = isCompleted,
            isDelayed = isDelayed,
            isInProgress = isInProgress
        )
    }

    private fun String.isCompletedStatus(): Boolean {
        return normalizedStatus() in setOf(
            "CONCLUIDO",
            "CONCLUIDA",
            "COMPLETO",
            "COMPLETA",
            "FINALIZADO",
            "FINALIZADA"
        )
    }

    private fun String.isInProgressStatus(): Boolean {
        return normalizedStatus() in setOf(
            "EM_PROGRESSO",
            "EMPROGRESSO",
            "EM_ANDAMENTO",
            "ANDAMENTO"
        )
    }

    private fun String.isPendingStatus(): Boolean {
        return normalizedStatus() in setOf(
            "PENDENTE",
            "PENDING",
            "POR_INICIAR"
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

    private fun String?.toUiDateText(): String {
        val date = this?.take(10)?.let { value ->
            runCatching { LocalDate.parse(value) }.getOrNull()
        } ?: return "-"

        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    private fun TarefaDto.toTaskStatusLabel(): String {
        return when {
            status.isCompletedStatus() -> "Concluída"
            isLate() -> "Atrasada"
            status.isInProgressStatus() -> "Em progresso"
            else -> "Pendente"
        }
    }

    private fun TarefaDto.isLate(): Boolean {
        val dueDate = data_fim?.toLocalDateOrNull() ?: return false
        return !status.isCompletedStatus() && dueDate.isBefore(LocalDate.now())
    }

    private fun String.parseProjectDateOrNull(): LocalDate? {
        val trimmed = trim()

        return try {
            LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (_: DateTimeParseException) {
            runCatching { LocalDate.parse(trimmed) }.getOrNull()
        }
    }
}
