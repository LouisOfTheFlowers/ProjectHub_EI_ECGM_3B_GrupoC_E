package com.example.projecthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.remote.supabase.UserRemoteDataSource
import com.example.projecthub.remote.supabase.models.ProjetoDto
import com.example.projecthub.repository.ProjetoRepository
import com.example.projecthub.repository.ProjetoUserRepository
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
    val id: Int,
    val name: String,
    val description: String,
    val coordinator: String,
    val managerId: Int?,
    val statusLabel: String,
    val startDate: String,
    val dueDate: String,
    val memberCount: Int,
    val isCompleted: Boolean,
    val isDelayed: Boolean,
    val isInProgress: Boolean
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
    val errorMessage: String? = null
)

class AdminProjectsViewModel(
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
    private val projetoUserRepository: ProjetoUserRepository = ProjetoUserRepository(),
    private val userRemoteDataSource: UserRemoteDataSource = UserRemoteDataSource()
) : ViewModel() {

    var state by mutableStateOf(AdminProjectsState())
        private set

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            val projectsResult = projetoRepository.getProjetos()
            val projectUsersResult = projetoUserRepository.getProjetoUsers()
            val usersResult = runCatching { userRemoteDataSource.getUsers() }

            if (projectsResult.isFailure || projectUsersResult.isFailure || usersResult.isFailure) {
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
                .filter { it.role.normalizedStatus() in setOf("ADMIN", "GESTOR", "COORDENADOR") }
                .ifEmpty { users }
                .mapNotNull { user ->
                    user.id?.let {
                        AdminProjectManager(
                            id = it,
                            name = user.nome.ifBlank { user.username }
                        )
                    }
                }
                .sortedBy { it.name }

            val projectMemberCounts = projectUsersResult.getOrDefault(emptyList())
                .groupingBy { it.projeto_id }
                .eachCount()
            val projects = projectsResult.getOrDefault(emptyList())
                .mapNotNull { projeto -> projeto.toListItem(usersById, projectMemberCounts) }
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
        val dueDate = data_fim?.toLocalDateOrNull()
        val isCompleted = status.isCompletedStatus()
        val isInProgress = status.isInProgressStatus()
        val isDelayed = !isCompleted && dueDate != null && dueDate.isBefore(LocalDate.now())

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

    private fun String.parseProjectDateOrNull(): LocalDate? {
        val trimmed = trim()

        return try {
            LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (_: DateTimeParseException) {
            runCatching { LocalDate.parse(trimmed) }.getOrNull()
        }
    }
}
