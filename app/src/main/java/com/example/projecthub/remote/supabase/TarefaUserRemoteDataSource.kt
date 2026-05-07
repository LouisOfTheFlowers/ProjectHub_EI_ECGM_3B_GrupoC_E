package com.example.projecthub.remote.supabase

import com.example.projecthub.remote.supabase.models.TarefaUserDto
import io.github.jan.supabase.postgrest.from

class TarefaUserRemoteDataSource {

    suspend fun getTarefaUsers(): List<TarefaUserDto> {
        return SupabaseClientProvider.client
            .from("tarefa_users")
            .select()
            .decodeAs<List<TarefaUserDto>>()
    }

    suspend fun getTarefaUserById(id: Int): TarefaUserDto? {
        return try {
            SupabaseClientProvider.client
                .from("tarefa_users")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeAs<List<TarefaUserDto>>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUsersByTarefa(tarefaId: Int): List<TarefaUserDto> {
        return SupabaseClientProvider.client
            .from("tarefa_users")
            .select {
                filter {
                    eq("tarefa_id", tarefaId)
                }
            }
            .decodeAs<List<TarefaUserDto>>()
    }

    suspend fun getTarefasByUser(userId: Int): List<TarefaUserDto> {
        return SupabaseClientProvider.client
            .from("tarefa_users")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeAs<List<TarefaUserDto>>()
    }

    suspend fun getTarefaUserByTarefaAndUser(
        tarefaId: Int,
        userId: Int
    ): TarefaUserDto? {
        return try {
            SupabaseClientProvider.client
                .from("tarefa_users")
                .select {
                    filter {
                        eq("tarefa_id", tarefaId)
                        eq("user_id", userId)
                    }
                }
                .decodeAs<List<TarefaUserDto>>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createTarefaUser(tarefaUser: TarefaUserDto) {
        SupabaseClientProvider.client
            .from("tarefa_users")
            .insert(tarefaUser)
    }

    suspend fun deleteTarefaUser(tarefaUserId: Int) {
        SupabaseClientProvider.client
            .from("tarefa_users")
            .delete {
                filter {
                    eq("id", tarefaUserId)
                }
            }
    }

    suspend fun deleteTarefaUserByTarefaAndUser(
        tarefaId: Int,
        userId: Int
    ) {
        SupabaseClientProvider.client
            .from("tarefa_users")
            .delete {
                filter {
                    eq("tarefa_id", tarefaId)
                    eq("user_id", userId)
                }
            }
    }
}