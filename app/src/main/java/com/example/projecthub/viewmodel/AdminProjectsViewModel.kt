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
import java.time.LocalDate

data class AdminProjectManager(
    val id: Int,
    val name: String
)

data class AdminProjectListItem(
    val id: Int,
    val name: String,
    val description: String,
    val status: String,
    val statusLabel: String,
    val coordinator: String,
    val managerId: Int?,
    val memberCount: Int,
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

data class AdminProjectsState(
    val projects: List<AdminProjectListItem> = emptyList(),
    val visibleProjects: List<AdminProjectListItem> = emptyList(),
    val managers: List<AdminProjectManager> = emptyList(),
    val coordinators: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedStatus: String = "Todos",
    val selectedCoordinator: String = "Todos",
    val completedCount: Int = 0,
    val inProgressCount: Int = 0,
    val delayedCount: Int = 0,
    val isLoading: Boolean = true,
    val isCreating: Boolean = false,
    val errorMessage: String? = null,
    val createErrorMessage: String? = null
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
            val usersResult = runCatching { userRemoteDataSource.getUsers() }
            val projectUsersResult = projetoUserRepository.getProjetoUsers()

            if (projectsResult.isFailure || usersResult.isFailure || projectUsersResult.isFailure) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar os projetos."
                )
                return@launch
            }

            val users = usersResult.getOrDefault(emptyList())
            val usersById = users.mapNotNull { user -> user.id?.let { it to user.nome } }.toMap()
            val projectUsers = projectUsersResult.getOrDefault(emptyList())
            val memberCountByProject = projectUsers.groupingBy { it.projeto_id }.eachCount()
            val managers = users
                .filter { it.role.equals("GESTOR", true) || it.role.equals("ADMIN", true) }
                .mapNotNull { user -> user.id?.let { AdminProjectManager(it, user.nome) } }
                .sortedBy { it.name.lowercase() }

            val projects = projectsResult.getOrDefault(emptyList())
                .mapNotNull { dto -> dto.toListItem(usersById, memberCountByProject) }
                .sortedBy { it.name.lowercase() }

            state = state.copy(
                projects = projects,
                managers = managers,
                coordinators = projects.map { it.coordinator }.distinct().sorted(),
                completedCount = projects.count { it.isCompleted },
                inProgressCount = projects.count { !it.isCompleted && !it.isDelayed },
                delayedCount = projects.count { it.isDelayed },
                isLoading = false
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
            state = state.copy(isCreating = true, createErrorMessage = null)
            val result = projetoRepository.createProjeto(name, description, startDateText, endDateText, managerId)
            state = state.copy(isCreating = false)

            if (result.isSuccess) {
                onSuccess()
                loadProjects()
            } else {
                state = state.copy(createErrorMessage = result.exceptionOrNull()?.message ?: "Erro ao criar projeto.")
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
        val currentProject = state.projects.firstOrNull { it.id == projectId }

        viewModelScope.launch {
            state = state.copy(isCreating = true, createErrorMessage = null)
            val result = projetoRepository.updateProjeto(
                projetoId = projectId,
                nome = name,
                descricao = description,
                dataInicio = startDateText,
                dataFim = endDateText,
                status = currentProject?.status ?: "PENDENTE",
                gestorId = managerId
            )
            state = state.copy(isCreating = false)

            if (result.isSuccess) {
                onSuccess()
                loadProjects()
            } else {
                state = state.copy(createErrorMessage = result.exceptionOrNull()?.message ?: "Erro ao guardar projeto.")
            }
        }
    }

    fun deleteProject(projectId: Int) {
        viewModelScope.launch {
            val result = projetoRepository.deleteProjeto(projectId)

            if (result.isSuccess) {
                loadProjects()
            } else {
                state = state.copy(errorMessage = result.exceptionOrNull()?.message ?: "Erro ao apagar projeto.")
            }
        }
    }

    private fun applyFilters() {
        val query = state.searchQuery.trim()
        val filtered = state.projects.filter { project ->
            val matchesQuery = query.isBlank() ||
                project.name.contains(query, true) ||
                project.description.contains(query, true)

            val matchesStatus = when (state.selectedStatus) {
                "Concluídos" -> project.isCompleted
                "Em progresso" -> !project.isCompleted && !project.isDelayed
                "Atrasados" -> project.isDelayed
                else -> true
            }

            val matchesCoordinator = state.selectedCoordinator == "Todos" ||
                project.coordinator == state.selectedCoordinator

            matchesQuery && matchesStatus && matchesCoordinator
        }

        state = state.copy(visibleProjects = filtered)
    }

    private fun ProjetoDto.toListItem(
        usersById: Map<Int, String>,
        memberCountByProject: Map<Int, Int>
    ): AdminProjectListItem? {
        val projectId = id ?: return null

        return AdminProjectListItem(
            id = projectId,
            name = nome,
            description = descricao?.takeIf { it.isNotBlank() } ?: "Sem descrição",
            status = status,
            statusLabel = status.replace("_", " "),
            coordinator = gestor_id?.let { usersById[it] } ?: "Sem gestor",
            managerId = gestor_id,
            memberCount = memberCountByProject[projectId] ?: 0,
            startDate = data_inicio ?: "-",
            dueDate = data_fim ?: "-"
        )
    }
}
