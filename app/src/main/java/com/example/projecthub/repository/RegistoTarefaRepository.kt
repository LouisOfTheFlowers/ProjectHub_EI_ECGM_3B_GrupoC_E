package com.example.projecthub.repository

import com.example.projecthub.remote.supabase.RegistoTarefaRemoteDataSource
import com.example.projecthub.remote.supabase.models.RegistoTarefaDto

class RegistoTarefaRepository(
    private val registoTarefaRemoteDataSource: RegistoTarefaRemoteDataSource = RegistoTarefaRemoteDataSource()
) {

    suspend fun getRegistos(): Result<List<RegistoTarefaDto>> {
        return try {
            Result.success(registoTarefaRemoteDataSource.getRegistos())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRegistoById(id: Int): Result<RegistoTarefaDto?> {
        return try {
            Result.success(registoTarefaRemoteDataSource.getRegistoById(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRegistosByTarefa(tarefaId: Int): Result<List<RegistoTarefaDto>> {
        return try {
            Result.success(registoTarefaRemoteDataSource.getRegistosByTarefa(tarefaId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRegistosByUser(userId: Int): Result<List<RegistoTarefaDto>> {
        return try {
            Result.success(registoTarefaRemoteDataSource.getRegistosByUser(userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRegistosByTarefaAndUser(
        tarefaId: Int,
        userId: Int
    ): Result<List<RegistoTarefaDto>> {
        return try {
            Result.success(
                registoTarefaRemoteDataSource.getRegistosByTarefaAndUser(
                    tarefaId = tarefaId,
                    userId = userId
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRegisto(
        tarefaId: Int,
        userId: Int,
        data: String,
        local: String?,
        taxaConclusao: Int,
        tempoGasto: Float?
    ): Result<Unit> {
        return try {
            if (data.isBlank()) {
                return Result.failure(Exception("A data do registo não pode estar vazia."))
            }

            if (taxaConclusao < 0 || taxaConclusao > 100) {
                return Result.failure(Exception("A taxa de conclusão deve estar entre 0 e 100."))
            }

            if (tempoGasto != null && tempoGasto < 0f) {
                return Result.failure(Exception("O tempo gasto não pode ser negativo."))
            }

            val registo = RegistoTarefaDto(
                id = null,
                tarefa_id = tarefaId,
                user_id = userId,
                data = data,
                local = local?.trim(),
                taxa_conclusao = taxaConclusao,
                tempo_gasto = tempoGasto,
                created_at = null
            )

            registoTarefaRemoteDataSource.createRegisto(registo)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRegisto(
        registoId: Int,
        tarefaId: Int,
        userId: Int,
        data: String,
        local: String?,
        taxaConclusao: Int,
        tempoGasto: Float?
    ): Result<Unit> {
        return try {
            if (data.isBlank()) {
                return Result.failure(Exception("A data do registo não pode estar vazia."))
            }

            if (taxaConclusao < 0 || taxaConclusao > 100) {
                return Result.failure(Exception("A taxa de conclusão deve estar entre 0 e 100."))
            }

            if (tempoGasto != null && tempoGasto < 0f) {
                return Result.failure(Exception("O tempo gasto não pode ser negativo."))
            }

            val registo = RegistoTarefaDto(
                id = registoId,
                tarefa_id = tarefaId,
                user_id = userId,
                data = data,
                local = local?.trim(),
                taxa_conclusao = taxaConclusao,
                tempo_gasto = tempoGasto,
                created_at = null
            )

            registoTarefaRemoteDataSource.updateRegisto(
                registoId = registoId,
                registo = registo
            )

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRegisto(registoId: Int): Result<Unit> {
        return try {
            registoTarefaRemoteDataSource.deleteRegisto(registoId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}