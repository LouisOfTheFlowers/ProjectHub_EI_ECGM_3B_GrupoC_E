package com.example.projecthub.remote.supabase

import com.example.projecthub.remote.supabase.models.ProjectMemberCountDto
import com.example.projecthub.remote.supabase.models.ProjetoUserDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc

class ProjetoUserRemoteDataSource {

    suspend fun getProjetoUsers(): List<ProjetoUserDto> {
        return SupabaseClientProvider.client
            .from("projeto_users")
            .select()
            .decodeAs<List<ProjetoUserDto>>()
    }

    suspend fun getProjectMemberCounts(): List<ProjectMemberCountDto> {
        return SupabaseClientProvider.client
            .postgrest
            .rpc("get_project_member_counts")
            .decodeAs<List<ProjectMemberCountDto>>()
    }

    suspend fun getProjetoUserById(id: Int): ProjetoUserDto? {
        return try {
            SupabaseClientProvider.client
                .from("projeto_users")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeAs<List<ProjetoUserDto>>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUsersByProjeto(projetoId: Int): List<ProjetoUserDto> {
        return SupabaseClientProvider.client
            .from("projeto_users")
            .select {
                filter {
                    eq("projeto_id", projetoId)
                }
            }
            .decodeAs<List<ProjetoUserDto>>()
    }

    suspend fun getProjetosByUser(userId: Int): List<ProjetoUserDto> {
        return SupabaseClientProvider.client
            .from("projeto_users")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeAs<List<ProjetoUserDto>>()
    }

    suspend fun getProjetoUserByProjetoAndUser(
        projetoId: Int,
        userId: Int
    ): ProjetoUserDto? {
        return try {
            SupabaseClientProvider.client
                .from("projeto_users")
                .select {
                    filter {
                        eq("projeto_id", projetoId)
                        eq("user_id", userId)
                    }
                }
                .decodeAs<List<ProjetoUserDto>>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createProjetoUser(projetoUser: ProjetoUserDto) {
        SupabaseClientProvider.client
            .from("projeto_users")
            .insert(projetoUser)
    }

    suspend fun deleteProjetoUser(projetoUserId: Int) {
        SupabaseClientProvider.client
            .from("projeto_users")
            .delete {
                filter {
                    eq("id", projetoUserId)
                }
            }
    }

    suspend fun deleteProjetoUserByProjetoAndUser(
        projetoId: Int,
        userId: Int
    ) {
        SupabaseClientProvider.client
            .from("projeto_users")
            .delete {
                filter {
                    eq("projeto_id", projetoId)
                    eq("user_id", userId)
                }
            }
    }
}
