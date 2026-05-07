package com.example.projecthub.repository

import com.example.projecthub.remote.supabase.ProjetoRemoteDataSource
import com.example.projecthub.remote.supabase.models.ProjetoDto

class ProjetoRepository(
    private val projetoRemoteDataSource: ProjetoRemoteDataSource = ProjetoRemoteDataSource()
) {

    suspend fun getProjetos(): Result<List<ProjetoDto>> {
        return try {
            Result.success(projetoRemoteDataSource.getProjetos())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProjetoById(id: Int): Result<ProjetoDto?> {
        return try {
            Result.success(projetoRemoteDataSource.getProjetoById(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProjetosByGestor(gestorId: Int): Result<List<ProjetoDto>> {
        return try {
            Result.success(projetoRemoteDataSource.getProjetosByGestor(gestorId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProjetosByStatus(status: String): Result<List<ProjetoDto>> {
        return try {
            Result.success(projetoRemoteDataSource.getProjetosByStatus(status))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProjeto(
        nome: String,
        descricao: String?,
        dataInicio: String?,
        dataFim: String?,
        gestorId: Int?
    ): Result<Unit> {
        return try {
            if (nome.isBlank()) {
                return Result.failure(Exception("O nome do projecto não pode estar vazio."))
            }

            val projeto = ProjetoDto(
                id = null,
                nome = nome.trim(),
                descricao = descricao?.trim(),
                data_inicio = dataInicio,
                data_fim = dataFim,
                status = "PENDENTE",
                gestor_id = gestorId,
                created_at = null
            )

            projetoRemoteDataSource.createProjeto(projeto)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProjeto(
        projetoId: Int,
        nome: String,
        descricao: String?,
        dataInicio: String?,
        dataFim: String?,
        status: String,
        gestorId: Int?
    ): Result<Unit> {
        return try {
            if (nome.isBlank()) {
                return Result.failure(Exception("O nome do projecto não pode estar vazio."))
            }

            val projeto = ProjetoDto(
                id = projetoId,
                nome = nome.trim(),
                descricao = descricao?.trim(),
                data_inicio = dataInicio,
                data_fim = dataFim,
                status = status,
                gestor_id = gestorId,
                created_at = null
            )

            projetoRemoteDataSource.updateProjeto(
                projetoId = projetoId,
                projeto = projeto
            )

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProjeto(projetoId: Int): Result<Unit> {
        return try {
            projetoRemoteDataSource.deleteProjeto(projetoId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun concluirProjeto(projetoId: Int): Result<Unit> {
        return try {
            projetoRemoteDataSource.concluirProjeto(projetoId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun associarGestorAoProjeto(
        projetoId: Int,
        gestorId: Int
    ): Result<Unit> {
        return try {
            projetoRemoteDataSource.associarGestorAoProjeto(
                projetoId = projetoId,
                gestorId = gestorId
            )

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}