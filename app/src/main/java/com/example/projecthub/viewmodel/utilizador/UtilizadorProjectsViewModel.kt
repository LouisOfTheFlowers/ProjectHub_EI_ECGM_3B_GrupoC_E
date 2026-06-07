package com.example.projecthub.viewmodel.utilizador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.remote.supabase.models.ProjetoDto
import com.example.projecthub.remote.supabase.models.TarefaDto
import com.example.projecthub.repository.ProjetoRepository
import com.example.projecthub.repository.ProjetoUserRepository
import com.example.projecthub.repository.TarefaRepository
import com.example.projecthub.repository.TarefaUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class UtilizadorProjectsState(
    val projects: List<UtilizadorProjectItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class UtilizadorProjectItem(
    val project: ProjetoDto,
    val tasksCount: Int,
    val completedTasks: Int,
    val lateTasks: Int,
    val completedTaskHistory: List<TarefaDto>
)

class UtilizadorProjectsViewModel(
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
    private val projetoUserRepository: ProjetoUserRepository = ProjetoUserRepository(),
    private val tarefaRepository: TarefaRepository = TarefaRepository(),
    private val tarefaUserRepository: TarefaUserRepository = TarefaUserRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(UtilizadorProjectsState())
    val stateFlow: StateFlow<UtilizadorProjectsState> = _state
    private var state: UtilizadorProjectsState
        get() = _state.value
        set(value) { _state.value = value }

    fun loadProjects(userId: Int?) {
        if (userId == null) {
            state = UtilizadorProjectsState(errorMessage = "Não foi possível identificar o utilizador autenticado.")
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            val taskAssociationsResult = tarefaUserRepository.getTarefasByUser(userId)
            if (taskAssociationsResult.isFailure) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar as tarefas do utilizador."
                )
                return@launch
            }

            val taskIds = taskAssociationsResult
                .getOrDefault(emptyList())
                .map { it.tarefa_id }
                .toSet()

            val tasksResult = tarefaRepository.getTarefas()
            val projectsResult = projetoRepository.getProjetos()
            val projectAssociationsResult = projetoUserRepository.getProjetosByUser(userId)

            if (tasksResult.isFailure || projectsResult.isFailure || projectAssociationsResult.isFailure) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar os projetos do utilizador."
                )
                return@launch
            }

            val today = LocalDate.now()
            val allProjects = projectsResult.getOrDefault(emptyList())
            val projectsById = allProjects
                .mapNotNull { project -> project.id?.let { it to project } }
                .toMap()
            val userTasks = tasksResult
                .getOrDefault(emptyList())
                .filter { it.id in taskIds }
            val associatedProjectIds = projectAssociationsResult
                .getOrDefault(emptyList())
                .map { it.projeto_id }
                .toSet() + userTasks.map { it.projeto_id }.toSet()
            val tasksByProject = userTasks.groupBy { it.projeto_id }
            val projectItems = associatedProjectIds
                .mapNotNull { projectId ->
                    val project = projectsById[projectId] ?: return@mapNotNull null
                    val projectTasks = tasksByProject[projectId].orEmpty()
                    UtilizadorProjectItem(
                        project = project,
                        tasksCount = projectTasks.size,
                        completedTasks = projectTasks.count { it.status.isCompletedStatus() },
                        lateTasks = projectTasks.count { it.isLate(today) },
                        completedTaskHistory = projectTasks
                            .filter { it.status.isCompletedStatus() }
                            .sortedByDescending { it.data_fim.orEmpty() }
                    )
                }
                .sortedBy { it.project.nome.lowercase() }

            state = UtilizadorProjectsState(projects = projectItems, isLoading = false)
        }
    }

    private fun TarefaDto.isLate(today: LocalDate): Boolean {
        if (status.isCompletedStatus()) return false
        val endDate = data_fim?.toLocalDateOrNull() ?: return false
        return endDate.isBefore(today)
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
        return try {
            LocalDate.parse(this)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
