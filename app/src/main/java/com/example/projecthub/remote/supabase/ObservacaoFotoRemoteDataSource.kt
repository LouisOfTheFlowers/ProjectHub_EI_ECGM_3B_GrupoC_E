package com.example.projecthub.remote.supabase

import com.example.projecthub.remote.supabase.models.ObservacaoFotoDto
import io.github.jan.supabase.postgrest.from

class ObservacaoFotoRemoteDataSource {

    suspend fun getFotos(): List<ObservacaoFotoDto> {
        return SupabaseClientProvider.client
            .from("observacao_fotos")
            .select()
            .decodeAs<List<ObservacaoFotoDto>>()
    }

    suspend fun getFotoById(id: Int): ObservacaoFotoDto? {
        return try {
            SupabaseClientProvider.client
                .from("observacao_fotos")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeAs<List<ObservacaoFotoDto>>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getFotosByObservacao(observacaoId: Int): List<ObservacaoFotoDto> {
        return SupabaseClientProvider.client
            .from("observacao_fotos")
            .select {
                filter {
                    eq("observacao_id", observacaoId)
                }
            }
            .decodeAs<List<ObservacaoFotoDto>>()
    }

    suspend fun createFoto(foto: ObservacaoFotoDto) {
        SupabaseClientProvider.client
            .from("observacao_fotos")
            .insert(foto)
    }

    suspend fun updateFoto(
        fotoId: Int,
        foto: ObservacaoFotoDto
    ) {
        SupabaseClientProvider.client
            .from("observacao_fotos")
            .update(foto) {
                filter {
                    eq("id", fotoId)
                }
            }
    }

    suspend fun deleteFoto(fotoId: Int) {
        SupabaseClientProvider.client
            .from("observacao_fotos")
            .delete {
                filter {
                    eq("id", fotoId)
                }
            }
    }
}