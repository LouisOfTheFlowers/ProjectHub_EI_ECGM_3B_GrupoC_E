package com.example.projecthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.repository.ProjetoRepository
import com.example.projecthub.repository.TarefaRepository
import kotlinx.coroutines.launch
import java.text.Normalizer

data class GestorDashboardState(
    val totalProjects: Int = 0,
    val completedTasks: Int = 0,
    val inProgressTasks: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class GestorDashboardViewModel(
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
    private val tarefaRepository: TarefaRepository = TarefaRepository()
) : ViewModel() {

    var state by mutableStateOf(GestorDashboardState())
        private set

    fun loadDashboard(gestorId: Int?) {
        if (gestorId == null) {
            state = GestorDashboardState(
                isLoading = false,
                errorMessage = "Não foi possível identificar o gestor autenticado."
            )
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            val projetosResult = projetoRepository.getProjetosByGestor(gestorId)
            if (projetosResult.isFailure) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar os dados da dashboard."
                )
                return@launch
            }

            val projetos = projetosResult.getOrDefault(emptyList())
            val projectIds = projetos.mapNotNull { it.id }.toSet()

            val tarefasResult = tarefaRepository.getTarefas()
            if (tarefasResult.isFailure) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar as tarefas do gestor."
                )
                return@launch
            }

            val managerTasks = tarefasResult
                .getOrDefault(emptyList())
                .filter { it.projeto_id in projectIds }

            state = GestorDashboardState(
                totalProjects = projetos.size,
                completedTasks = managerTasks.count { it.status.isCompletedStatus() },
                inProgressTasks = managerTasks.count { it.status.isInProgressStatus() },
                isLoading = false
            )
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
            "IN_PROGRESS",
            "INPROGRESS",
            "A_DECORRER"
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
