package com.example.projecthub.repository

import com.example.projecthub.remote.supabase.ProjetoUserRemoteDataSource
import com.example.projecthub.remote.supabase.models.ProjetoUserDto

class ProjetoUserRepository(
    private val projetoUserRemoteDataSource: ProjetoUserRemoteDataSource = ProjetoUserRemoteDataSource()
) {

    suspend fun getProjetoUsers(): Result<List<ProjetoUserDto>> {
        return try {
            Result.success(projetoUserRemoteDataSource.getProjetoUsers())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProjetoUserById(id: Int): Result<ProjetoUserDto?> {
        return try {
            Result.success(projetoUserRemoteDataSource.getProjetoUserById(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsersByProjeto(projetoId: Int): Result<List<ProjetoUserDto>> {
        return try {
            Result.success(projetoUserRemoteDataSource.getUsersByProjeto(projetoId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProjetosByUser(userId: Int): Result<List<ProjetoUserDto>> {
        return try {
            Result.success(projetoUserRemoteDataSource.getProjetosByUser(userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun associarUserAoProjeto(
        projetoId: Int,
        userId: Int
    ): Result<Unit> {
        return try {
            val existingAssociation =
                projetoUserRemoteDataSource.getProjetoUserByProjetoAndUser(
                    projetoId = projetoId,
                    userId = userId
                )

            if (existingAssociation != null) {
                return Result.failure(Exception("Este utilizador já está associado a este projecto."))
            }

            val projetoUser = ProjetoUserDto(
                id = null,
                projeto_id = projetoId,
                user_id = userId
            )

            projetoUserRemoteDataSource.createProjetoUser(projetoUser)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removerUserDoProjeto(
        projetoId: Int,
        userId: Int
    ): Result<Unit> {
        return try {
            projetoUserRemoteDataSource.deleteProjetoUserByProjetoAndUser(
                projetoId = projetoId,
                userId = userId
            )

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProjetoUser(projetoUserId: Int): Result<Unit> {
        return try {
            projetoUserRemoteDataSource.deleteProjetoUser(projetoUserId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}