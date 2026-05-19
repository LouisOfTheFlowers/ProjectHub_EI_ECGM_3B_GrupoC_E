package com.example.projecthub.remote.supabase

import com.example.projecthub.remote.supabase.models.TarefaDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class TarefaRemoteDataSource {

    @Serializable
    private data class ManagerDeleteTaskParams(
        @SerialName("p_task_id")
        val taskId: Int
    )

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

    suspend fun createTarefaReturning(tarefa: TarefaDto): TarefaDto {
        return SupabaseClientProvider.client
            .from("tarefas")
            .insert(tarefa) {
                select()
            }
            .decodeSingle<TarefaDto>()
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

    suspend fun deleteManagerTask(tarefaId: Int) {
        SupabaseClientProvider.client
            .postgrest
            .rpc("manager_delete_task", ManagerDeleteTaskParams(tarefaId))
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
