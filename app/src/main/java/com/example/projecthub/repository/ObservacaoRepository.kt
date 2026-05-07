package com.example.projecthub.repository

import com.example.projecthub.remote.supabase.ObservacaoRemoteDataSource
import com.example.projecthub.remote.supabase.models.ObservacaoDto

class ObservacaoRepository(
    private val observacaoRemoteDataSource: ObservacaoRemoteDataSource = ObservacaoRemoteDataSource()
) {

    suspend fun getObservacoes(): Result<List<ObservacaoDto>> {
        return try {
            Result.success(observacaoRemoteDataSource.getObservacoes())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getObservacaoById(id: Int): Result<ObservacaoDto?> {
        return try {
            Result.success(observacaoRemoteDataSource.getObservacaoById(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getObservacoesByRegisto(registoId: Int): Result<List<ObservacaoDto>> {
        return try {
            Result.success(observacaoRemoteDataSource.getObservacoesByRegisto(registoId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createObservacao(
        registoId: Int,
        texto: String
    ): Result<Unit> {
        return try {
            if (texto.isBlank()) {
                return Result.failure(Exception("A observação não pode estar vazia."))
            }

            val observacao = ObservacaoDto(
                id = null,
                registo_id = registoId,
                texto = texto.trim(),
                created_at = null
            )

            observacaoRemoteDataSource.createObservacao(observacao)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateObservacao(
        observacaoId: Int,
        registoId: Int,
        texto: String
    ): Result<Unit> {
        return try {
            if (texto.isBlank()) {
                return Result.failure(Exception("A observação não pode estar vazia."))
            }

            val observacao = ObservacaoDto(
                id = observacaoId,
                registo_id = registoId,
                texto = texto.trim(),
                created_at = null
            )

            observacaoRemoteDataSource.updateObservacao(
                observacaoId = observacaoId,
                observacao = observacao
            )

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteObservacao(observacaoId: Int): Result<Unit> {
        return try {
            observacaoRemoteDataSource.deleteObservacao(observacaoId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}