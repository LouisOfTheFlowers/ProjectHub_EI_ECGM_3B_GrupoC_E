package com.example.projecthub.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.projecthub.remote.supabase.models.UserDto

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nome: String,
    val username: String,
    val email: String,
    val password: String = "",
    val foto: String? = null,
    val role: String,
    val createdAt: String? = null,
    val status: String
) {
    fun toDto(): UserDto {
        return UserDto(
            id = id,
            nome = nome,
            username = username,
            email = email,
            password = password,
            foto = foto,
            role = role,
            createdAt = createdAt,
            status = status
        )
    }
}
