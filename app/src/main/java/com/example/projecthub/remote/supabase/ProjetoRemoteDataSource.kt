package com.example.projecthub.remote.supabase

import com.example.projecthub.remote.supabase.models.ProjetoDto
import io.github.jan.supabase.postgrest.from

class ProjetoRemoteDataSource {

    suspend fun getProjetos(): List<ProjetoDto> {
        return SupabaseClientProvider.client
            .from("projetos")
            .select()
            .decodeAs<List<ProjetoDto>>()
    }

    suspend fun getProjetoById(id: Int): ProjetoDto? {
        return try {
            SupabaseClientProvider.client
                .from("projetos")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeAs<List<ProjetoDto>>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getProjetosByGestor(gestorId: Int): List<ProjetoDto> {
        return SupabaseClientProvider.client
            .from("projetos")
            .select {
                filter {
                    eq("gestor_id", gestorId)
                }
            }
            .decodeAs<List<ProjetoDto>>()
    }

    suspend fun getProjetosByStatus(status: String): List<ProjetoDto> {
        return SupabaseClientProvider.client
            .from("projetos")
            .select {
                filter {
                    eq("status", status)
                }
            }
            .decodeAs<List<ProjetoDto>>()
    }

    suspend fun createProjeto(projeto: ProjetoDto) {
        SupabaseClientProvider.client
            .from("projetos")
            .insert(projeto)
    }

    suspend fun updateProjeto(
        projetoId: Int,
        projeto: ProjetoDto
    ) {
        SupabaseClientProvider.client
            .from("projetos")
            .update(projeto) {
                filter {
                    eq("id", projetoId)
                }
            }
    }

    suspend fun deleteProjeto(projetoId: Int) {
        SupabaseClientProvider.client
            .from("projetos")
            .delete {
                filter {
                    eq("id", projetoId)
                }
            }
    }

    suspend fun concluirProjeto(projetoId: Int) {
        SupabaseClientProvider.client
            .from("projetos")
            .update(
                mapOf("status" to "CONCLUIDO")
            ) {
                filter {
                    eq("id", projetoId)
                }
            }
    }

    suspend fun associarGestorAoProjeto(
        projetoId: Int,
        gestorId: Int
    ) {
        SupabaseClientProvider.client
            .from("projetos")
            .update(
                mapOf("gestor_id" to gestorId)
            ) {
                filter {
                    eq("id", projetoId)
                }
            }
    }
}