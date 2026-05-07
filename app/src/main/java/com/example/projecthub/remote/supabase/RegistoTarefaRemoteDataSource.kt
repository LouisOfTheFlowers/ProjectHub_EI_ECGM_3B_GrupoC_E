package com.example.projecthub.remote.supabase

import com.example.projecthub.remote.supabase.models.RegistoTarefaDto
import io.github.jan.supabase.postgrest.from

class RegistoTarefaRemoteDataSource {

    suspend fun getRegistos(): List<RegistoTarefaDto> {
        return SupabaseClientProvider.client
            .from("registos_tarefa")
            .select()
            .decodeAs<List<RegistoTarefaDto>>()
    }

    suspend fun getRegistoById(id: Int): RegistoTarefaDto? {
        return try {
            SupabaseClientProvider.client
                .from("registos_tarefa")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeAs<List<RegistoTarefaDto>>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getRegistosByTarefa(tarefaId: Int): List<RegistoTarefaDto> {
        return SupabaseClientProvider.client
            .from("registos_tarefa")
            .select {
                filter {
                    eq("tarefa_id", tarefaId)
                }
            }
            .decodeAs<List<RegistoTarefaDto>>()
    }

    suspend fun getRegistosByUser(userId: Int): List<RegistoTarefaDto> {
        return SupabaseClientProvider.client
            .from("registos_tarefa")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeAs<List<RegistoTarefaDto>>()
    }

    suspend fun getRegistosByTarefaAndUser(
        tarefaId: Int,
        userId: Int
    ): List<RegistoTarefaDto> {
        return SupabaseClientProvider.client
            .from("registos_tarefa")
            .select {
                filter {
                    eq("tarefa_id", tarefaId)
                    eq("user_id", userId)
                }
            }
            .decodeAs<List<RegistoTarefaDto>>()
    }

    suspend fun createRegisto(registo: RegistoTarefaDto) {
        SupabaseClientProvider.client
            .from("registos_tarefa")
            .insert(registo)
    }

    suspend fun updateRegisto(
        registoId: Int,
        registo: RegistoTarefaDto
    ) {
        SupabaseClientProvider.client
            .from("registos_tarefa")
            .update(registo) {
                filter {
                    eq("id", registoId)
                }
            }
    }

    suspend fun deleteRegisto(registoId: Int) {
        SupabaseClientProvider.client
            .from("registos_tarefa")
            .delete {
                filter {
                    eq("id", registoId)
                }
            }
    }
}