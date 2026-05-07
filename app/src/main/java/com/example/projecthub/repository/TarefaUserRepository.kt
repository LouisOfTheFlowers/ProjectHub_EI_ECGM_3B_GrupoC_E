package com.example.projecthub.repository

import com.example.projecthub.remote.supabase.TarefaUserRemoteDataSource
import com.example.projecthub.remote.supabase.models.TarefaUserDto

class TarefaUserRepository(
    private val tarefaUserRemoteDataSource: TarefaUserRemoteDataSource = TarefaUserRemoteDataSource()
) {

    suspend fun getTarefaUsers(): Result<List<TarefaUserDto>> {
        return try {
            Result.success(tarefaUserRemoteDataSource.getTarefaUsers())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTarefaUserById(id: Int): Result<TarefaUserDto?> {
        return try {
            Result.success(tarefaUserRemoteDataSource.getTarefaUserById(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsersByTarefa(tarefaId: Int): Result<List<TarefaUserDto>> {
        return try {
            Result.success(tarefaUserRemoteDataSource.getUsersByTarefa(tarefaId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTarefasByUser(userId: Int): Result<List<TarefaUserDto>> {
        return try {
            Result.success(tarefaUserRemoteDataSource.getTarefasByUser(userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun associarUserATarefa(
        tarefaId: Int,
        userId: Int
    ): Result<Unit> {
        return try {
            val existingAssociation =
                tarefaUserRemoteDataSource.getTarefaUserByTarefaAndUser(
                    tarefaId = tarefaId,
                    userId = userId
                )

            if (existingAssociation != null) {
                return Result.failure(Exception("Este utilizador já está associado a esta tarefa."))
            }

            val tarefaUser = TarefaUserDto(
                id = null,
                tarefa_id = tarefaId,
                user_id = userId
            )

            tarefaUserRemoteDataSource.createTarefaUser(tarefaUser)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removerUserDaTarefa(
        tarefaId: Int,
        userId: Int
    ): Result<Unit> {
        return try {
            tarefaUserRemoteDataSource.deleteTarefaUserByTarefaAndUser(
                tarefaId = tarefaId,
                userId = userId
            )

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTarefaUser(tarefaUserId: Int): Result<Unit> {
        return try {
            tarefaUserRemoteDataSource.deleteTarefaUser(tarefaUserId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}