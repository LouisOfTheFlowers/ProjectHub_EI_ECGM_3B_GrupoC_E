package com.example.projecthub.remote.supabase

import com.example.projecthub.remote.supabase.models.ObservacaoDto
import io.github.jan.supabase.postgrest.from

class ObservacaoRemoteDataSource {

    suspend fun getObservacoes(): List<ObservacaoDto> {
        return SupabaseClientProvider.client
            .from("observacoes")
            .select()
            .decodeAs<List<ObservacaoDto>>()
    }

    suspend fun getObservacaoById(id: Int): ObservacaoDto? {
        return try {
            SupabaseClientProvider.client
                .from("observacoes")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeAs<List<ObservacaoDto>>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getObservacoesByRegisto(registoId: Int): List<ObservacaoDto> {
        return SupabaseClientProvider.client
            .from("observacoes")
            .select {
                filter {
                    eq("registo_id", registoId)
                }
            }
            .decodeAs<List<ObservacaoDto>>()
    }

    suspend fun createObservacao(observacao: ObservacaoDto) {
        SupabaseClientProvider.client
            .from("observacoes")
            .insert(observacao)
    }

    suspend fun updateObservacao(
        observacaoId: Int,
        observacao: ObservacaoDto
    ) {
        SupabaseClientProvider.client
            .from("observacoes")
            .update(observacao) {
                filter {
                    eq("id", observacaoId)
                }
            }
    }

    suspend fun deleteObservacao(observacaoId: Int) {
        SupabaseClientProvider.client
            .from("observacoes")
            .delete {
                filter {
                    eq("id", observacaoId)
                }
            }
    }
}