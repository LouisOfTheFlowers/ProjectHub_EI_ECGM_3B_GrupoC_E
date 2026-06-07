package com.example.projecthub.viewmodel.admin

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.remote.supabase.UserRemoteDataSource
import com.example.projecthub.remote.supabase.models.UserDto
import com.example.projecthub.repository.ProjetoRepository
import com.example.projecthub.repository.ProjetoUserRepository
import kotlinx.coroutines.launch

val AdminTeamEditableRoles = listOf("GESTOR", "UTILIZADOR")

data class AdminTeamProjectOption(
    val id: Int,
    val name: String
)

data class AdminTeamUserItem(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val role: String,
    val status: String,
    val projectNames: List<String>,
    val source: UserDto
) {
    val isActive: Boolean
        get() = status.equals("ATIVO", ignoreCase = true)
}

data class AdminTeamsState(
    val users: List<AdminTeamUserItem> = emptyList(),
    val visibleUsers: List<AdminTeamUserItem> = emptyList(),
    val projects: List<AdminTeamProjectOption> = emptyList(),
    val roles: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedRole: String? = null,
    val selectedProjectId: Int? = null,
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val isLoading: Boolean = true,
    val updatingRoleUserId: Int? = null,
    val deletingUserId: Int? = null,
    val errorMessage: String? = null
)

class AdminTeamsViewModel(
    private val userRemoteDataSource: UserRemoteDataSource = UserRemoteDataSource(),
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
    private val projetoUserRepository: ProjetoUserRepository = ProjetoUserRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AdminTeamsState())
    val stateFlow: StateFlow<AdminTeamsState> = _state
    private var state: AdminTeamsState
        get() = _state.value
        set(value) { _state.value = value }

    init {
        loadTeams()
    }

    fun loadTeams() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            val usersResult = runCatching { userRemoteDataSource.getUsers() }
            val projectsResult = projetoRepository.getProjetos()
            val projectUsersResult = projetoUserRepository.getProjetoUsers()

            if (usersResult.isFailure || projectsResult.isFailure || projectUsersResult.isFailure) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar a gestão de equipas."
                )
                return@launch
            }

            val users = usersResult.getOrDefault(emptyList())
            val projects = projectsResult.getOrDefault(emptyList())
            val projectUsers = projectUsersResult.getOrDefault(emptyList())

            val projectsById = projects
                .mapNotNull { projeto -> projeto.id?.let { it to projeto.nome } }
                .toMap()

            val projectNamesByUserId = projectUsers
                .groupBy { it.user_id }
                .mapValues { (_, links) ->
                    links.mapNotNull { link -> projectsById[link.projeto_id] }.distinct().sorted()
                }

            val teamUsers = users
                .filterNot { user -> user.role.equals("ADMIN", ignoreCase = true) }
                .mapNotNull { user ->
                    val userId = user.id ?: return@mapNotNull null

                    AdminTeamUserItem(
                        id = userId,
                        name = user.nome,
                        username = user.username,
                        email = user.email,
                        role = user.role,
                        status = user.status,
                        projectNames = projectNamesByUserId[userId].orEmpty(),
                        source = user
                    )
                }
                .sortedWith(compareBy<AdminTeamUserItem> { !it.isActive }.thenBy { it.name.lowercase() })

            state = state.copy(
                users = teamUsers,
                projects = projects
                    .mapNotNull { projeto -> projeto.id?.let { AdminTeamProjectOption(it, projeto.nome) } }
                    .sortedBy { it.name.lowercase() },
                roles = (teamUsers.map { it.role } + AdminTeamEditableRoles).distinct().sorted(),
                totalUsers = teamUsers.size,
                activeUsers = teamUsers.count { it.isActive },
                isLoading = false
            )
            applyFilters()
        }
    }

    fun updateSearchQuery(query: String) {
        state = state.copy(searchQuery = query)
        applyFilters()
    }

    fun updateRoleFilter(role: String?) {
        state = state.copy(selectedRole = role)
        applyFilters()
    }

    fun updateProjectFilter(projectId: Int?) {
        state = state.copy(selectedProjectId = projectId)
        applyFilters()
    }

    fun updateUserRole(user: AdminTeamUserItem, role: String) {
        if (user.role.equals(role, ignoreCase = true)) {
            return
        }

        viewModelScope.launch {
            state = state.copy(updatingRoleUserId = user.id, errorMessage = null)

            val updatedUser = user.source.copy(role = role)
            val result = runCatching {
                userRemoteDataSource.updateUserRole(user.id, role)
            }

            if (result.isSuccess) {
                val updatedItem = user.copy(
                    role = role,
                    source = updatedUser
                )
                val updatedUsers = state.users.map { item ->
                    if (item.id == user.id) updatedItem else item
                }
                state = state.copy(
                    users = updatedUsers,
                    roles = (updatedUsers.map { it.role } + AdminTeamEditableRoles).distinct().sorted(),
                    updatingRoleUserId = null
                )
                applyFilters()
            } else {
                state = state.copy(
                    updatingRoleUserId = null,
                    errorMessage = result.exceptionOrNull()?.message
                        ?: "Não foi possível atualizar a role do utilizador."
                )
            }
        }
    }

    fun deleteUser(user: AdminTeamUserItem) {
        viewModelScope.launch {
            state = state.copy(deletingUserId = user.id, errorMessage = null)

            val result = runCatching {
                userRemoteDataSource.deleteUser(user.id)
            }

            if (result.isSuccess) {
                loadTeams()
            } else {
                state = state.copy(
                    deletingUserId = null,
                    errorMessage = userErrorMessage(
                        result.exceptionOrNull(),
                        fallback = "Não foi possível remover o utilizador."
                    )
                )
            }
        }
    }

    private fun applyFilters() {
        val query = state.searchQuery.trim()
        val selectedRole = state.selectedRole
        val selectedProject = state.selectedProjectId
        val projectName = selectedProject?.let { id ->
            state.projects.firstOrNull { it.id == id }?.name
        }

        val filteredUsers = state.users.filter { user ->
            val matchesQuery = query.isBlank() ||
                user.name.contains(query, ignoreCase = true) ||
                user.username.contains(query, ignoreCase = true) ||
                user.email.contains(query, ignoreCase = true)

            val matchesRole = selectedRole == null ||
                user.role.equals(selectedRole, ignoreCase = true)

            val matchesProject = projectName == null ||
                user.projectNames.any { it.equals(projectName, ignoreCase = true) }

            matchesQuery && matchesRole && matchesProject
        }

        state = state.copy(visibleUsers = filteredUsers)
    }

    private fun userErrorMessage(error: Throwable?, fallback: String): String {
        val rawMessage = error?.message.orEmpty()

        return when {
            rawMessage.contains("admin_delete_user", ignoreCase = true) ||
                rawMessage.contains("function", ignoreCase = true) ->
                "Aplica a migration de remoção de utilizadores no Supabase antes de apagar utilizadores."

            rawMessage.contains("Only admins", ignoreCase = true) ||
                rawMessage.contains("Apenas administradores", ignoreCase = true) ->
                "Apenas administradores podem remover utilizadores."

            rawMessage.contains("própria conta", ignoreCase = true) ||
                rawMessage.contains("propria conta", ignoreCase = true) ->
                "Não podes remover a tua própria conta de administrador."

            rawMessage.isBlank() -> fallback

            else -> rawMessage
                .lineSequence()
                .firstOrNull()
                ?.take(160)
                ?: fallback
        }
    }
}
