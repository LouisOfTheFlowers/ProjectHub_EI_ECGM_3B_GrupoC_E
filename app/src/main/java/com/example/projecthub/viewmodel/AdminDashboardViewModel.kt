package com.example.projecthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.remote.supabase.UserRemoteDataSource
import com.example.projecthub.repository.ProjetoRepository
import kotlinx.coroutines.launch
import java.text.Normalizer

data class AdminDashboardState(
    val completedProjects: Int = 0,
    val activeUsers: Int = 0,
    val pendingProjects: Int = 0,
    val totalProjects: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class AdminDashboardViewModel(
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
    private val userRemoteDataSource: UserRemoteDataSource = UserRemoteDataSource()
) : ViewModel() {

    var state by mutableStateOf(AdminDashboardState())
        private set

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            val projetosResult = projetoRepository.getProjetos()
            val usersResult = runCatching { userRemoteDataSource.getUsers() }

            if (projetosResult.isFailure || usersResult.isFailure) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar os dados da dashboard."
                )
                return@launch
            }

            val projetos = projetosResult.getOrDefault(emptyList())
            val users = usersResult.getOrDefault(emptyList())
            val completedProjects = projetos.count { it.status.isCompletedStatus() }

            state = AdminDashboardState(
                completedProjects = completedProjects,
                activeUsers = users.count { it.status.isActiveStatus() },
                pendingProjects = projetos.size - completedProjects,
                totalProjects = projetos.size,
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

    private fun String.isActiveStatus(): Boolean {
        return normalizedStatus() in setOf("ATIVO", "ACTIVO")
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
