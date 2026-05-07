package com.example.projecthub.repository

import com.example.projecthub.remote.supabase.AvaliacaoRemoteDataSource
import com.example.projecthub.remote.supabase.models.AvaliacaoDto

class AvaliacaoRepository(
    private val avaliacaoRemoteDataSource: AvaliacaoRemoteDataSource = AvaliacaoRemoteDataSource()
) {

    suspend fun getAvaliacoes(): Result<List<AvaliacaoDto>> {
        return try {
            Result.success(avaliacaoRemoteDataSource.getAvaliacoes())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvaliacaoById(id: Int): Result<AvaliacaoDto?> {
        return try {
            Result.success(avaliacaoRemoteDataSource.getAvaliacaoById(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvaliacoesByProjeto(projetoId: Int): Result<List<AvaliacaoDto>> {
        return try {
            Result.success(avaliacaoRemoteDataSource.getAvaliacoesByProjeto(projetoId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvaliacoesByUser(userId: Int): Result<List<AvaliacaoDto>> {
        return try {
            Result.success(avaliacaoRemoteDataSource.getAvaliacoesByUser(userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvaliacaoByProjetoAndUser(
        projetoId: Int,
        userId: Int
    ): Result<AvaliacaoDto?> {
        return try {
            Result.success(
                avaliacaoRemoteDataSource.getAvaliacaoByProjetoAndUser(
                    projetoId = projetoId,
                    userId = userId
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAvaliacao(
        projetoId: Int,
        userId: Int,
        classificacao: Int,
        comentario: String?
    ): Result<Unit> {
        return try {
            if (classificacao < 0 || classificacao > 100) {
                return Result.failure(Exception("A classificação deve estar entre 0 e 100."))
            }

            val existingAvaliacao =
                avaliacaoRemoteDataSource.getAvaliacaoByProjetoAndUser(
                    projetoId = projetoId,
                    userId = userId
                )

            if (existingAvaliacao != null) {
                return Result.failure(Exception("Este utilizador já foi avaliado neste projecto."))
            }

            val avaliacao = AvaliacaoDto(
                id = null,
                projeto_id = projetoId,
                user_id = userId,
                classificacao = classificacao,
                comentario = comentario
            )

            avaliacaoRemoteDataSource.createAvaliacao(avaliacao)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAvaliacao(
        avaliacaoId: Int,
        projetoId: Int,
        userId: Int,
        classificacao: Int,
        comentario: String?
    ): Result<Unit> {
        return try {
            if (classificacao < 0 || classificacao > 100) {
                return Result.failure(Exception("A classificação deve estar entre 0 e 100."))
            }

            val avaliacao = AvaliacaoDto(
                id = avaliacaoId,
                projeto_id = projetoId,
                user_id = userId,
                classificacao = classificacao,
                comentario = comentario
            )

            avaliacaoRemoteDataSource.updateAvaliacao(
                avaliacaoId = avaliacaoId,
                avaliacao = avaliacao
            )

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAvaliacao(avaliacaoId: Int): Result<Unit> {
        return try {
            avaliacaoRemoteDataSource.deleteAvaliacao(avaliacaoId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}