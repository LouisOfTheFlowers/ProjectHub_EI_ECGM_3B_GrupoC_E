package com.example.projecthub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecthub.local.database.DatabaseProvider
import com.example.projecthub.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DatabaseProvider.getDatabase(application)

    private val repository = UserRepository(
        userDao = database.userDao(),
        syncQueueDao = database.syncQueueDao()
    )

    var message: String = ""
        private set

    var isLoggedIn: Boolean = false
        private set

    fun register(
        nome: String,
        username: String,
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.register(nome, username, email, password)

            if (result.isSuccess) {
                onResult(true, "Conta criada com sucesso.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Erro ao criar conta.")
            }
        }
    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.login(email, password)

            if (result.isSuccess) {
                isLoggedIn = true
                onResult(true, "Login efetuado com sucesso.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Erro ao iniciar sessão.")
            }
        }
    }
}