package com.example.projecthub.viewmodel.utilizador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.remote.supabase.models.TarefaDto
import com.example.projecthub.repository.TarefaRepository
import com.example.projecthub.repository.TarefaUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class UtilizadorDashboardState(
    val inProgressTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val lateTasks: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class UtilizadorDashboardViewModel(
    private val tarefaRepository: TarefaRepository = TarefaRepository(),
    private val tarefaUserRepository: TarefaUserRepository = TarefaUserRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(UtilizadorDashboardState())
    val stateFlow: StateFlow<UtilizadorDashboardState> = _state
    private var state: UtilizadorDashboardState
        get() = _state.value
        set(value) { _state.value = value }

    fun loadDashboard(userId: Int?) {
        if (userId == null) {
            state = UtilizadorDashboardState(
                errorMessage = "Não foi possível identificar o utilizador autenticado."
            )
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            val associationsResult = tarefaUserRepository.getTarefasByUser(userId)
            if (associationsResult.isFailure) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar as tarefas do utilizador."
                )
                return@launch
            }

            val taskIds = associationsResult
                .getOrDefault(emptyList())
                .map { it.tarefa_id }
                .toSet()

            val tasksResult = tarefaRepository.getTarefas()
            if (tasksResult.isFailure) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar os detalhes das tarefas."
                )
                return@launch
            }

            val today = LocalDate.now()
            val userTasks = tasksResult
                .getOrDefault(emptyList())
                .filter { it.id in taskIds }

            state = UtilizadorDashboardState(
                inProgressTasks = userTasks.count { it.isInProgress(today) },
                completedTasks = userTasks.count { it.status.isCompletedStatus() },
                pendingTasks = userTasks.count { it.isPending(today) },
                lateTasks = userTasks.count { it.isLate(today) },
                isLoading = false
            )
        }
    }

    private fun TarefaDto.isPending(today: LocalDate): Boolean {
        if (status.isCompletedStatus()) return false
        val startDate = data_inicio?.toLocalDateOrNull()
        return status.isPendingStatus() || (startDate != null && startDate.isAfter(today))
    }

    private fun TarefaDto.isInProgress(today: LocalDate): Boolean {
        if (status.isCompletedStatus()) return false
        val startDate = data_inicio?.toLocalDateOrNull()
        val hasStarted = startDate == null || !startDate.isAfter(today)
        return hasStarted && (status.isInProgressStatus() || status.isPendingStatus())
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

    private fun String.isInProgressStatus(): Boolean {
        return normalizedStatus() in setOf(
            "EM_PROGRESSO",
            "EMPROGRESSO",
            "IN_PROGRESS",
            "INPROGRESS",
            "A_DECORRER"
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
        return try {
            LocalDate.parse(this)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
