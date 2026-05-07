package com.example.projecthub.remote.supabase

import com.example.projecthub.remote.supabase.models.AvaliacaoDto
import io.github.jan.supabase.postgrest.from

class AvaliacaoRemoteDataSource {

    suspend fun getAvaliacoes(): List<AvaliacaoDto> {
        return SupabaseClientProvider.client
            .from("avaliacoes")
            .select()
            .decodeAs<List<AvaliacaoDto>>()
    }

    suspend fun getAvaliacaoById(id: Int): AvaliacaoDto? {
        return try {
            SupabaseClientProvider.client
                .from("avaliacoes")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeAs<List<AvaliacaoDto>>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAvaliacoesByProjeto(projetoId: Int): List<AvaliacaoDto> {
        return SupabaseClientProvider.client
            .from("avaliacoes")
            .select {
                filter {
                    eq("projeto_id", projetoId)
                }
            }
            .decodeAs<List<AvaliacaoDto>>()
    }

    suspend fun getAvaliacoesByUser(userId: Int): List<AvaliacaoDto> {
        return SupabaseClientProvider.client
            .from("avaliacoes")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeAs<List<AvaliacaoDto>>()
    }

    suspend fun getAvaliacaoByProjetoAndUser(
        projetoId: Int,
        userId: Int
    ): AvaliacaoDto? {
        return try {
            SupabaseClientProvider.client
                .from("avaliacoes")
                .select {
                    filter {
                        eq("projeto_id", projetoId)
                        eq("user_id", userId)
                    }
                }
                .decodeAs<List<AvaliacaoDto>>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createAvaliacao(avaliacao: AvaliacaoDto) {
        SupabaseClientProvider.client
            .from("avaliacoes")
            .insert(avaliacao)
    }

    suspend fun updateAvaliacao(
        avaliacaoId: Int,
        avaliacao: AvaliacaoDto
    ) {
        SupabaseClientProvider.client
            .from("avaliacoes")
            .update(avaliacao) {
                filter {
                    eq("id", avaliacaoId)
                }
            }
    }

    suspend fun deleteAvaliacao(avaliacaoId: Int) {
        SupabaseClientProvider.client
            .from("avaliacoes")
            .delete {
                filter {
                    eq("id", avaliacaoId)
                }
            }
    }
}