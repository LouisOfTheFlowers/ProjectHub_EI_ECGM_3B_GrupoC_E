package com.example.projecthub.repository

import com.example.projecthub.remote.supabase.ObservacaoFotoRemoteDataSource
import com.example.projecthub.remote.supabase.models.ObservacaoFotoDto

class ObservacaoFotoRepository(
    private val observacaoFotoRemoteDataSource: ObservacaoFotoRemoteDataSource = ObservacaoFotoRemoteDataSource()
) {

    suspend fun getFotos(): Result<List<ObservacaoFotoDto>> {
        return try {
            Result.success(observacaoFotoRemoteDataSource.getFotos())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFotoById(id: Int): Result<ObservacaoFotoDto?> {
        return try {
            Result.success(observacaoFotoRemoteDataSource.getFotoById(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFotosByObservacao(observacaoId: Int): Result<List<ObservacaoFotoDto>> {
        return try {
            Result.success(observacaoFotoRemoteDataSource.getFotosByObservacao(observacaoId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFoto(
        observacaoId: Int,
        fotoUrl: String
    ): Result<Unit> {
        return try {
            if (fotoUrl.isBlank()) {
                return Result.failure(Exception("A fotografia não pode estar vazia."))
            }

            val foto = ObservacaoFotoDto(
                id = null,
                observacao_id = observacaoId,
                foto_url = fotoUrl.trim()
            )

            observacaoFotoRemoteDataSource.createFoto(foto)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFoto(
        fotoId: Int,
        observacaoId: Int,
        fotoUrl: String
    ): Result<Unit> {
        return try {
            if (fotoUrl.isBlank()) {
                return Result.failure(Exception("A fotografia não pode estar vazia."))
            }

            val foto = ObservacaoFotoDto(
                id = fotoId,
                observacao_id = observacaoId,
                foto_url = fotoUrl.trim()
            )

            observacaoFotoRemoteDataSource.updateFoto(
                fotoId = fotoId,
                foto = foto
            )

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFoto(fotoId: Int): Result<Unit> {
        return try {
            observacaoFotoRemoteDataSource.deleteFoto(fotoId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}