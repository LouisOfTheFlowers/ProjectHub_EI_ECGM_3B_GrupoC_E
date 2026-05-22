package com.example.projecthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.remote.supabase.UserRemoteDataSource
import com.example.projecthub.remote.supabase.models.AvaliacaoDto
import com.example.projecthub.remote.supabase.models.TarefaDto
import com.example.projecthub.repository.AvaliacaoRepository
import com.example.projecthub.repository.ProjetoRepository
import com.example.projecthub.repository.ProjetoUserRepository
import com.example.projecthub.repository.TarefaRepository
import com.example.projecthub.repository.TarefaUserRepository
import kotlinx.coroutines.launch
import java.text.Normalizer

data class GestorTeamProjectOption(
    val id: Int,
    val name: String
)

data class GestorTeamMemberItem(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val projectNames: List<String>,
    val projectIds: Set<Int>,
    val averageRating: Double?,
    val completedTasks: Int,
    val averageRatingByProject: Map<Int, Double?>,
    val completedTasksByProject: Map<Int, Int>
)

data class GestorTeamState(
    val members: List<GestorTeamMemberItem> = emptyList(),
    val visibleMembers: List<GestorTeamMemberItem> = emptyList(),
    val projects: List<GestorTeamProjectOption> = emptyList(),
    val searchQuery: String = "",
    val selectedProjectId: Int? = null,
    val totalMembers: Int = 0,
    val averageRating: Double? = null,
    val completedTasks: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class GestorTeamViewModel(
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
    private val projetoUserRepository: ProjetoUserRepository = ProjetoUserRepository(),
    private val tarefaRepository: TarefaRepository = TarefaRepository(),
    private val tarefaUserRepository: TarefaUserRepository = TarefaUserRepository(),
    private val avaliacaoRepository: AvaliacaoRepository = AvaliacaoRepository(),
    private val userRemoteDataSource: UserRemoteDataSource = UserRemoteDataSource()
) : ViewModel() {

    private data class MemberSource(
        val item: GestorTeamMemberItem,
        val projectIds: Set<Int>
    )

    private var sourceMembers: List<MemberSource> = emptyList()

    var state by mutableStateOf(GestorTeamState())
        private set

    fun loadTeam(gestorId: Int?) {
        if (gestorId == null) {
            state = GestorTeamState(
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
            val ratingsResult = avaliacaoRepository.getAvaliacoes()
            val usersResult = runCatching { userRemoteDataSource.getUsers() }

            if (
                projectsResult.isFailure ||
                tasksResult.isFailure ||
                taskUsersResult.isFailure ||
                ratingsResult.isFailure ||
                usersResult.isFailure
            ) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Nao foi possivel carregar a equipa do gestor."
                )
                return@launch
            }

            val projects = projectsResult.getOrDefault(emptyList())
            val projectOptions = projects
                .mapNotNull { project -> project.id?.let { GestorTeamProjectOption(it, project.nome) } }
                .sortedBy { it.name.lowercase() }
            val projectIds = projectOptions.map { it.id }.toSet()
            val projectsById = projectOptions.associate { it.id to it.name }

            val projectUsers = projectIds.flatMap { projectId ->
                projetoUserRepository.getUsersByProjeto(projectId).getOrDefault(emptyList())
            }
            val projectIdsByUser = projectUsers
                .groupBy { it.user_id }
                .mapValues { entry -> entry.value.map { it.projeto_id }.toSet() }

            val usersById = usersResult.getOrDefault(emptyList())
                .mapNotNull { user -> user.id?.let { it to user } }
                .toMap()

            val tasksById = tasksResult.getOrDefault(emptyList())
                .filter { it.projeto_id in projectIds }
                .mapNotNull { task -> task.id?.let { it to task } }
                .toMap()
            val projectIdsByTaskUser = taskUsersResult.getOrDefault(emptyList())
                .mapNotNull { taskUser ->
                    tasksById[taskUser.tarefa_id]?.let { task ->
                        taskUser.user_id to task.projeto_id
                    }
                }
                .groupBy({ it.first }, { it.second })
                .mapValues { entry -> entry.value.toSet() }
            val completedTaskIds = tasksById
                .filterValues { it.status.isCompletedStatus() }
                .keys

            val completedTasksByUser = taskUsersResult.getOrDefault(emptyList())
                .filter { it.tarefa_id in completedTaskIds }
                .groupBy { it.user_id }
                .mapValues { entry -> entry.value.map { it.tarefa_id }.distinct().size }
            val completedTasksByUserAndProject = taskUsersResult.getOrDefault(emptyList())
                .mapNotNull { taskUser ->
                    val task = tasksById[taskUser.tarefa_id] ?: return@mapNotNull null
                    if (task.id !in completedTaskIds) return@mapNotNull null
                    Triple(taskUser.user_id, task.projeto_id, taskUser.tarefa_id)
                }
                .groupBy { it.first to it.second }
                .mapValues { entry -> entry.value.map { it.third }.distinct().size }

            val ratingsByUser = ratingsResult.getOrDefault(emptyList())
                .filter { it.projeto_id in projectIds }
                .groupBy { it.user_id }
            val ratingsByUserAndProject = ratingsResult.getOrDefault(emptyList())
                .filter { it.projeto_id in projectIds }
                .groupBy { it.user_id to it.projeto_id }
                .mapValues { entry -> entry.value.averageRatingOrNull() }

            val workedProjectIdsByUser = (projectIdsByUser.keys + projectIdsByTaskUser.keys)
                .associateWith { userId ->
                    projectIdsByUser[userId].orEmpty() + projectIdsByTaskUser[userId].orEmpty()
                }

            sourceMembers = workedProjectIdsByUser.mapNotNull { (userId, userProjectIds) ->
                val user = usersById[userId] ?: return@mapNotNull null
                val ratings = ratingsByUser[userId].orEmpty()
                val averageRating = ratings.averageRatingOrNull()
                val member = GestorTeamMemberItem(
                    id = userId,
                    name = user.nome.ifBlank { user.username },
                    username = user.username,
                    email = user.email,
                    projectNames = userProjectIds.mapNotNull { projectsById[it] }.sorted(),
                    projectIds = userProjectIds,
                    averageRating = averageRating,
                    completedTasks = completedTasksByUser[userId] ?: 0,
                    averageRatingByProject = userProjectIds.associateWith { projectId ->
                        ratingsByUserAndProject[userId to projectId]
                    },
                    completedTasksByProject = userProjectIds.associateWith { projectId ->
                        completedTasksByUserAndProject[userId to projectId] ?: 0
                    }
                )

                MemberSource(
                    item = member,
                    projectIds = userProjectIds
                )
            }.sortedBy { it.item.name.lowercase() }

            val allRatings = sourceMembers.mapNotNull { it.item.averageRating }
            state = state.copy(
                projects = projectOptions,
                totalMembers = sourceMembers.size,
                averageRating = allRatings.takeIf { it.isNotEmpty() }?.average(),
                completedTasks = sourceMembers.sumOf { it.item.completedTasks },
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

    fun updateProjectFilter(projectId: Int?) {
        state = state.copy(selectedProjectId = projectId)
        applyFilters()
    }

    private fun applyFilters() {
        val query = state.searchQuery.trim()
        val selectedProjectId = state.selectedProjectId

        val visibleMembers = sourceMembers.filter { source ->
            val member = source.item
            val matchesSearch = query.isBlank() ||
                member.name.contains(query, ignoreCase = true) ||
                member.username.contains(query, ignoreCase = true) ||
                member.email.contains(query, ignoreCase = true) ||
                member.projectNames.any { it.contains(query, ignoreCase = true) }

            val matchesProject = selectedProjectId == null || selectedProjectId in source.projectIds

            matchesSearch && matchesProject
        }.map { it.item }

        state = state.copy(visibleMembers = visibleMembers)
    }

    private fun List<AvaliacaoDto>.averageRatingOrNull(): Double? {
        if (isEmpty()) return null

        return map { it.classificacao.coerceIn(0, 5) }.average()
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
}
