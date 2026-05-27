package com.example.projecthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.remote.supabase.UserRemoteDataSource
import com.example.projecthub.remote.supabase.models.ProjetoDto
import com.example.projecthub.remote.supabase.models.TarefaDto
import com.example.projecthub.repository.AvaliacaoRepository
import com.example.projecthub.repository.ObservacaoRepository
import com.example.projecthub.repository.ProjetoRepository
import com.example.projecthub.repository.ProjetoUserRepository
import com.example.projecthub.repository.RegistoTarefaRepository
import com.example.projecthub.repository.TarefaRepository
import com.example.projecthub.repository.TarefaUserRepository
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class GestorProjectMember(
    val id: Int,
    val name: String,
    val email: String,
    val rating: Int? = null
)

data class GestorUserOption(
    val id: Int,
    val name: String,
    val email: String
)

data class GestorProjectListItem(
    val id: Int,
    val name: String,
    val description: String,
    val statusLabel: String,
    val startDate: String,
    val dueDate: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val inProgressTasks: Int,
    val pendingTasks: Int,
    val members: List<GestorProjectMember>,
    val progressPercent: Int,
    val isCompleted: Boolean,
    val isExpanded: Boolean = false
)

data class GestorProjectInfoParticipant(
    val id: Int,
    val name: String,
    val email: String,
    val rating: Int?,
    val comment: String?
)

data class GestorProjectInfoObservation(
    val id: Int?,
    val text: String,
    val userName: String,
    val date: String,
    val completionPercent: Int,
    val spentHours: Float?
)

data class GestorProjectInfoTask(
    val id: Int,
    val title: String,
    val description: String,
    val statusLabel: String,
    val startDate: String,
    val dueDate: String,
    val assignees: List<String>,
    val observations: List<GestorProjectInfoObservation>
)

data class GestorProjectInfoState(
    val project: GestorProjectListItem? = null,
    val participants: List<GestorProjectInfoParticipant> = emptyList(),
    val tasks: List<GestorProjectInfoTask> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class GestorProjectsState(
    val projects: List<GestorProjectListItem> = emptyList(),
    val visibleProjects: List<GestorProjectListItem> = emptyList(),
    val userOptions: List<GestorUserOption> = emptyList(),
    val searchQuery: String = "",
    val selectedStatus: String = "Todos os Status",
    val isLoading: Boolean = true,
    val isAssociating: Boolean = false,
    val errorMessage: String? = null,
    val actionMessage: String? = null,
    val detailState: GestorProjectInfoState = GestorProjectInfoState()
)

class GestorProjectsViewModel(
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
    private val tarefaRepository: TarefaRepository = TarefaRepository(),
    private val projetoUserRepository: ProjetoUserRepository = ProjetoUserRepository(),
    private val avaliacaoRepository: AvaliacaoRepository = AvaliacaoRepository(),
    private val tarefaUserRepository: TarefaUserRepository = TarefaUserRepository(),
    private val registoTarefaRepository: RegistoTarefaRepository = RegistoTarefaRepository(),
    private val observacaoRepository: ObservacaoRepository = ObservacaoRepository(),
    private val userRemoteDataSource: UserRemoteDataSource = UserRemoteDataSource()
) : ViewModel() {

    var state by mutableStateOf(GestorProjectsState())
        private set

    fun loadProjects(gestorId: Int?) {
        if (gestorId == null) {
            state = GestorProjectsState(
                isLoading = false,
                errorMessage = "Não foi possível identificar o gestor autenticado."
            )
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null, actionMessage = null)

            val projectsResult = projetoRepository.getProjetosByGestor(gestorId)
            val tasksResult = tarefaRepository.getTarefas()
            val ratingsResult = avaliacaoRepository.getAvaliacoes()
            val usersResult = runCatching { userRemoteDataSource.getUsers() }

            if (
                projectsResult.isFailure ||
                tasksResult.isFailure ||
                ratingsResult.isFailure ||
                usersResult.isFailure
            ) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar os projetos do gestor."
                )
                return@launch
            }

            val projects = projectsResult.getOrDefault(emptyList())
            val projectIds = projects.mapNotNull { it.id }.toSet()
            val projectUsers = projectIds.flatMap { projectId ->
                projetoUserRepository.getUsersByProjeto(projectId).getOrDefault(emptyList())
            }
            val tasksByProject = tasksResult
                .getOrDefault(emptyList())
                .filter { it.projeto_id in projectIds }
                .groupBy { it.projeto_id }
            val projectUsersByProject = projectUsers
                .filter { it.projeto_id in projectIds }
                .groupBy { it.projeto_id }
            val ratingsByProjectAndUser = ratingsResult
                .getOrDefault(emptyList())
                .filter { it.projeto_id in projectIds }
                .associateBy { it.projeto_id to it.user_id }
            val users = usersResult.getOrDefault(emptyList())
            val usersById = users.mapNotNull { user -> user.id?.let { it to user } }.toMap()
            val userOptions = users
                .filter { it.role.normalizedStatus() == "UTILIZADOR" }
                .mapNotNull { user ->
                    user.id?.let {
                        GestorUserOption(
                            id = it,
                            name = user.nome.ifBlank { user.username },
                            email = user.email
                        )
                    }
                }
                .sortedBy { it.name.lowercase() }

            val oldExpandedIds = state.projects.filter { it.isExpanded }.map { it.id }.toSet()
            val projectItems = projects
                .mapNotNull { project ->
                    project.toListItem(
                        tasks = tasksByProject[project.id].orEmpty(),
                        memberIds = projectUsersByProject[project.id].orEmpty().map { it.user_id },
                        usersById = usersById,
                        ratingsByProjectAndUser = ratingsByProjectAndUser,
                        isExpanded = project.id in oldExpandedIds
                    )
                }
                .sortedBy { it.name.lowercase() }

            state = state.copy(
                projects = projectItems,
                userOptions = userOptions,
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

    fun toggleProject(projectId: Int) {
        state = state.copy(
            projects = state.projects.map { project ->
                if (project.id == projectId) project.copy(isExpanded = !project.isExpanded) else project
            }
        )
        applyFilters()
    }

    fun loadProjectInfo(project: GestorProjectListItem) {
        viewModelScope.launch {
            state = state.copy(
                detailState = GestorProjectInfoState(
                    project = project,
                    isLoading = true
                )
            )

            val tasksResult = tarefaRepository.getTarefasByProjeto(project.id)
            val projectUsersResult = projetoUserRepository.getUsersByProjeto(project.id)
            val taskUsersResult = tarefaUserRepository.getTarefaUsers()
            val ratingsResult = avaliacaoRepository.getAvaliacoesByProjeto(project.id)
            val recordsResult = registoTarefaRepository.getRegistos()
            val observationsResult = observacaoRepository.getObservacoes()
            val usersResult = runCatching { userRemoteDataSource.getUsers() }

            if (
                tasksResult.isFailure ||
                projectUsersResult.isFailure ||
                taskUsersResult.isFailure ||
                ratingsResult.isFailure ||
                recordsResult.isFailure ||
                observationsResult.isFailure ||
                usersResult.isFailure
            ) {
                state = state.copy(
                    detailState = GestorProjectInfoState(
                        project = project,
                        isLoading = false,
                        errorMessage = "Não foi possível carregar os detalhes do projeto."
                    )
                )
                return@launch
            }

            val usersById = usersResult.getOrDefault(emptyList())
                .mapNotNull { user -> user.id?.let { it to user } }
                .toMap()
            val ratingsByUser = ratingsResult.getOrDefault(emptyList()).associateBy { it.user_id }
            val participants = projectUsersResult
                .getOrDefault(emptyList())
                .mapNotNull { relation ->
                    val user = usersById[relation.user_id] ?: return@mapNotNull null
                    val rating = ratingsByUser[relation.user_id]
                    GestorProjectInfoParticipant(
                        id = relation.user_id,
                        name = user.nome.ifBlank { user.username },
                        email = user.email,
                        rating = rating?.classificacao,
                        comment = rating?.comentario
                    )
                }
                .sortedBy { it.name.lowercase() }

            val tasks = tasksResult.getOrDefault(emptyList()).sortedBy { it.titulo.lowercase() }
            val taskIds = tasks.mapNotNull { it.id }.toSet()
            val taskUsersByTask = taskUsersResult
                .getOrDefault(emptyList())
                .filter { it.tarefa_id in taskIds }
                .groupBy { it.tarefa_id }
            val records = recordsResult
                .getOrDefault(emptyList())
                .filter { it.tarefa_id in taskIds }
            val recordsById = records.mapNotNull { record -> record.id?.let { it to record } }.toMap()
            val observationsByTask = observationsResult
                .getOrDefault(emptyList())
                .mapNotNull { observation ->
                    val record = recordsById[observation.registo_id] ?: return@mapNotNull null
                    val user = usersById[record.user_id]
                    val userName = user?.nome?.ifBlank { user.username } ?: "Utilizador ${record.user_id}"
                    record.tarefa_id to GestorProjectInfoObservation(
                        id = observation.id,
                        text = observation.texto,
                        userName = userName,
                        date = (observation.created_at ?: record.created_at ?: record.data).toUiDateText(),
                        completionPercent = record.taxa_conclusao,
                        spentHours = record.tempo_gasto
                    )
                }
                .groupBy({ it.first }, { it.second })

            val infoTasks = tasks.mapNotNull { task ->
                val taskId = task.id ?: return@mapNotNull null
                val assignees = taskUsersByTask[taskId]
                    .orEmpty()
                    .mapNotNull { relation ->
                        usersById[relation.user_id]?.let { user -> user.nome.ifBlank { user.username } }
                    }
                    .sortedBy { it.lowercase() }

                GestorProjectInfoTask(
                    id = taskId,
                    title = task.titulo,
                    description = task.descricao?.takeIf { it.isNotBlank() } ?: "Sem descrição",
                    statusLabel = task.toTaskStatusLabel(),
                    startDate = task.data_inicio.toUiDateText(),
                    dueDate = task.data_fim.toUiDateText(),
                    assignees = assignees,
                    observations = observationsByTask[taskId].orEmpty()
                        .sortedByDescending { it.date }
                )
            }

            state = state.copy(
                detailState = GestorProjectInfoState(
                    project = project,
                    participants = participants,
                    tasks = infoTasks,
                    isLoading = false
                )
            )
        }
    }

    fun clearProjectInfo() {
        state = state.copy(detailState = GestorProjectInfoState())
    }

    fun associateUserToProject(
        projetoId: Int,
        userId: Int?,
        gestorId: Int?
    ) {
        if (userId == null) {
            state = state.copy(actionMessage = null, errorMessage = "Seleciona um utilizador.")
            return
        }

        viewModelScope.launch {
            state = state.copy(isAssociating = true, errorMessage = null, actionMessage = null)

            val result = projetoUserRepository.associarUserAoProjeto(
                projetoId = projetoId,
                userId = userId
            )

            if (result.isSuccess) {
                state = state.copy(
                    isAssociating = false,
                    actionMessage = "Utilizador associado ao projeto."
                )
                loadProjects(gestorId)
            } else {
                state = state.copy(
                    isAssociating = false,
                    errorMessage = result.exceptionOrNull()?.message
                        ?: "Não foi possível associar o utilizador."
                )
            }
        }
    }

    fun completeProjectWithRatings(
        project: GestorProjectListItem,
        ratings: Map<Int, Int>,
        gestorId: Int?
    ) {
        viewModelScope.launch {
            state = state.copy(isAssociating = true, errorMessage = null, actionMessage = null)

            val invalidMember = project.members.firstOrNull { member ->
                ratings[member.id] == null || ratings[member.id] !in 0..5
            }

            if (invalidMember != null) {
                state = state.copy(
                    isAssociating = false,
                    errorMessage = "Avalia todos os membros de 0 a 5 estrelas."
                )
                return@launch
            }

            for (member in project.members) {
                val result = avaliacaoRepository.saveAvaliacao(
                    projetoId = project.id,
                    userId = member.id,
                    classificacao = ratings[member.id] ?: 0
                )

                if (result.isFailure) {
                    state = state.copy(
                        isAssociating = false,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Não foi possível guardar as avaliações."
                    )
                    return@launch
                }
            }

            val completeResult = projetoRepository.concluirProjeto(project.id)

            if (completeResult.isSuccess) {
                state = state.copy(
                    isAssociating = false,
                    actionMessage = "Projeto concluído com avaliações guardadas."
                )
                loadProjects(gestorId)
            } else {
                state = state.copy(
                    isAssociating = false,
                    errorMessage = completeResult.exceptionOrNull()?.message
                        ?: "Não foi possível concluir o projeto."
                )
            }
        }
    }

    fun clearMessages() {
        state = state.copy(errorMessage = null, actionMessage = null)
    }

    private fun applyFilters() {
        val query = state.searchQuery.trim()
        val visibleProjects = state.projects.filter { project ->
            val matchesSearch = query.isBlank() ||
                project.name.contains(query, ignoreCase = true) ||
                project.description.contains(query, ignoreCase = true) ||
                project.members.any { it.name.contains(query, ignoreCase = true) }

            val matchesStatus = when (state.selectedStatus) {
                "Concluídos" -> project.statusLabel == "Concluído"
                "Em Progresso" -> project.statusLabel == "Em Progresso"
                "Pendentes" -> project.statusLabel == "Pendente"
                else -> true
            }

            matchesSearch && matchesStatus
        }

        state = state.copy(visibleProjects = visibleProjects)
    }

    private fun ProjetoDto.toListItem(
        tasks: List<TarefaDto>,
        memberIds: List<Int>,
        usersById: Map<Int, com.example.projecthub.remote.supabase.models.UserDto>,
        ratingsByProjectAndUser: Map<Pair<Int, Int>, com.example.projecthub.remote.supabase.models.AvaliacaoDto>,
        isExpanded: Boolean
    ): GestorProjectListItem? {
        val projectId = id ?: return null
        val isCompleted = status.isCompletedStatus()
        val completedTasks = tasks.count { it.status.isCompletedStatus() }
        val pendingTasks = tasks.count { it.isPendingByDate() }
        val inProgressTasks = tasks.count { it.status.isInProgressStatus() && !it.isPendingByDate() }
        val progressPercent = if (tasks.isEmpty()) {
            0
        } else {
            ((completedTasks.toFloat() / tasks.size.toFloat()) * 100).toInt()
        }

        val members = memberIds.distinct().mapNotNull { userId ->
            usersById[userId]?.let { user ->
                GestorProjectMember(
                    id = userId,
                    name = user.nome.ifBlank { user.username },
                    email = user.email,
                    rating = ratingsByProjectAndUser[projectId to userId]?.classificacao
                )
            }
        }.sortedBy { it.name.lowercase() }

        return GestorProjectListItem(
            id = projectId,
            name = nome,
            description = descricao?.takeIf { it.isNotBlank() } ?: "Sem descrição",
            statusLabel = status.toStatusLabel(data_inicio),
            startDate = data_inicio?.take(10) ?: "-",
            dueDate = data_fim?.take(10) ?: "-",
            totalTasks = tasks.size,
            completedTasks = completedTasks,
            inProgressTasks = inProgressTasks,
            pendingTasks = pendingTasks,
            members = members,
            progressPercent = progressPercent,
            isCompleted = isCompleted,
            isExpanded = isExpanded
        )
    }

    private fun TarefaDto.isPendingByDate(): Boolean {
        val startDate = data_inicio?.take(10)?.toLocalDateOrNull()
        return status.isPendingStatus() || startDate?.isAfter(LocalDate.now()) == true
    }

    private fun TarefaDto.toTaskStatusLabel(): String {
        val dueDate = data_fim?.take(10)?.toLocalDateOrNull()
        val startDate = data_inicio?.take(10)?.toLocalDateOrNull()
        val isCompleted = status.isCompletedStatus()
        val isDelayed = !isCompleted && dueDate != null && dueDate.isBefore(LocalDate.now())
        val isInProgress = !isCompleted && !isDelayed && (
            status.isInProgressStatus() ||
                status.isPendingStatus() ||
                startDate?.isEqual(LocalDate.now()) == true ||
                startDate?.isBefore(LocalDate.now()) == true
            )

        return when {
            isCompleted -> "Concluída"
            isDelayed -> "Atrasada"
            isInProgress -> "Em progresso"
            else -> "Pendente"
        }
    }

    private fun String.toStatusLabel(startDateText: String?): String {
        val startDate = startDateText?.take(10)?.toLocalDateOrNull()
        return when {
            isCompletedStatus() -> "Concluído"
            isInProgressStatus() -> "Em Progresso"
            startDate == LocalDate.now() -> "Em Progresso"
            isPendingStatus() -> "Pendente"
            else -> this.ifBlank { "Sem status" }
        }
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
            "ANDAMENTO",
            "A_DECORRER",
            "IN_PROGRESS",
            "INPROGRESS"
        )
    }

    private fun String.isPendingStatus(): Boolean {
        return normalizedStatus() in setOf("PENDENTE", "POR_INICIAR", "NAO_INICIADO")
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
        return runCatching { LocalDate.parse(this) }.getOrNull()
    }

    private fun String?.toUiDateText(): String {
        val date = this?.take(10)?.toLocalDateOrNull() ?: return "-"
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
}
