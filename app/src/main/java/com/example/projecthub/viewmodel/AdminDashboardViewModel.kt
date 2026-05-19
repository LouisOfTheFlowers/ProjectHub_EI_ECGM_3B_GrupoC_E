package com.example.projecthub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.remote.supabase.UserRemoteDataSource
import com.example.projecthub.repository.ProjetoRepository
import kotlinx.coroutines.launch

data class AdminDashboardState(
    val completedProjects: Int = 0,
    val pendingProjects: Int = 0,
    val activeUsers: Int = 0,
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

            val projectsResult = projetoRepository.getProjetos()
            val usersResult = runCatching { userRemoteDataSource.getUsers() }

            if (projectsResult.isFailure || usersResult.isFailure) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar a dashboard."
                )
                return@launch
            }

            val projects = projectsResult.getOrDefault(emptyList())
            val users = usersResult.getOrDefault(emptyList())

            state = AdminDashboardState(
                completedProjects = projects.count { it.status.equals("CONCLUIDO", ignoreCase = true) },
                pendingProjects = projects.count { !it.status.equals("CONCLUIDO", ignoreCase = true) },
                activeUsers = users.count { it.status.equals("ATIVO", ignoreCase = true) },
                isLoading = false
            )
        }
    }
}
