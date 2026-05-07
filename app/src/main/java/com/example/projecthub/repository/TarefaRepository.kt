package com.example.projecthub.repository

import com.example.projecthub.remote.supabase.TarefaRemoteDataSource
import com.example.projecthub.remote.supabase.models.TarefaDto

class TarefaRepository(
    private val tarefaRemoteDataSource: TarefaRemoteDataSource = TarefaRemoteDataSource()
) {

    suspend fun getTarefas(): Result<List<TarefaDto>> {
        return try {
            Result.success(tarefaRemoteDataSource.getTarefas())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTarefaById(id: Int): Result<TarefaDto?> {
        return try {
            Result.success(tarefaRemoteDataSource.getTarefaById(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTarefasByProjeto(projetoId: Int): Result<List<TarefaDto>> {
        return try {
            Result.success(tarefaRemoteDataSource.getTarefasByProjeto(projetoId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTarefasByStatus(status: String): Result<List<TarefaDto>> {
        return try {
            Result.success(tarefaRemoteDataSource.getTarefasByStatus(status))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTarefasByProjetoAndStatus(
        projetoId: Int,
        status: String
    ): Result<List<TarefaDto>> {
        return try {
            Result.success(
                tarefaRemoteDataSource.getTarefasByProjetoAndStatus(
                    projetoId = projetoId,
                    status = status
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTarefa(
        titulo: String,
        descricao: String?,
        projetoId: Int,
        dataInicio: String?,
        dataFim: String?
    ): Result<Unit> {
        return try {
            if (titulo.isBlank()) {
                return Result.failure(Exception("O título da tarefa não pode estar vazio."))
            }

            val tarefa = TarefaDto(
                id = null,
                titulo = titulo.trim(),
                descricao = descricao?.trim(),
                projeto_id = projetoId,
                status = "PENDENTE",
                data_inicio = dataInicio,
                data_fim = dataFim
            )

            tarefaRemoteDataSource.createTarefa(tarefa)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTarefa(
        tarefaId: Int,
        titulo: String,
        descricao: String?,
        projetoId: Int,
        status: String,
        dataInicio: String?,
        dataFim: String?
    ): Result<Unit> {
        return try {
            if (titulo.isBlank()) {
                return Result.failure(Exception("O título da tarefa não pode estar vazio."))
            }

            val tarefa = TarefaDto(
                id = tarefaId,
                titulo = titulo.trim(),
                descricao = descricao?.trim(),
                projeto_id = projetoId,
                status = status,
                data_inicio = dataInicio,
                data_fim = dataFim
            )

            tarefaRemoteDataSource.updateTarefa(
                tarefaId = tarefaId,
                tarefa = tarefa
            )

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTarefa(tarefaId: Int): Result<Unit> {
        return try {
            tarefaRemoteDataSource.deleteTarefa(tarefaId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun concluirTarefa(tarefaId: Int): Result<Unit> {
        return try {
            tarefaRemoteDataSource.concluirTarefa(tarefaId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun atualizarStatusTarefa(
        tarefaId: Int,
        status: String
    ): Result<Unit> {
        return try {
            if (status.isBlank()) {
                return Result.failure(Exception("O estado da tarefa não pode estar vazio."))
            }

            tarefaRemoteDataSource.atualizarStatusTarefa(
                tarefaId = tarefaId,
                status = status
            )

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}