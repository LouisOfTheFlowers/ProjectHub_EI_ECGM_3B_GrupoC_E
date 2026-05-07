package com.example.projecthub.remote.supabase

import com.example.projecthub.remote.supabase.models.TarefaDto
import io.github.jan.supabase.postgrest.from

class TarefaRemoteDataSource {

    suspend fun getTarefas(): List<TarefaDto> {
        return SupabaseClientProvider.client
            .from("tarefas")
            .select()
            .decodeAs<List<TarefaDto>>()
    }

    suspend fun getTarefaById(id: Int): TarefaDto? {
        return try {
            SupabaseClientProvider.client
                .from("tarefas")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeAs<List<TarefaDto>>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getTarefasByProjeto(projetoId: Int): List<TarefaDto> {
        return SupabaseClientProvider.client
            .from("tarefas")
            .select {
                filter {
                    eq("projeto_id", projetoId)
                }
            }
            .decodeAs<List<TarefaDto>>()
    }

    suspend fun getTarefasByStatus(status: String): List<TarefaDto> {
        return SupabaseClientProvider.client
            .from("tarefas")
            .select {
                filter {
                    eq("status", status)
                }
            }
            .decodeAs<List<TarefaDto>>()
    }

    suspend fun getTarefasByProjetoAndStatus(
        projetoId: Int,
        status: String
    ): List<TarefaDto> {
        return SupabaseClientProvider.client
            .from("tarefas")
            .select {
                filter {
                    eq("projeto_id", projetoId)
                    eq("status", status)
                }
            }
            .decodeAs<List<TarefaDto>>()
    }

    suspend fun createTarefa(tarefa: TarefaDto) {
        SupabaseClientProvider.client
            .from("tarefas")
            .insert(tarefa)
    }

    suspend fun updateTarefa(
        tarefaId: Int,
        tarefa: TarefaDto
    ) {
        SupabaseClientProvider.client
            .from("tarefas")
            .update(tarefa) {
                filter {
                    eq("id", tarefaId)
                }
            }
    }

    suspend fun deleteTarefa(tarefaId: Int) {
        SupabaseClientProvider.client
            .from("tarefas")
            .delete {
                filter {
                    eq("id", tarefaId)
                }
            }
    }

    suspend fun concluirTarefa(tarefaId: Int) {
        SupabaseClientProvider.client
            .from("tarefas")
            .update(
                mapOf("status" to "CONCLUIDO")
            ) {
                filter {
                    eq("id", tarefaId)
                }
            }
    }

    suspend fun atualizarStatusTarefa(
        tarefaId: Int,
        status: String
    ) {
        SupabaseClientProvider.client
            .from("tarefas")
            .update(
                mapOf("status" to status)
            ) {
                filter {
                    eq("id", tarefaId)
                }
            }
    }
}