package com.example.projecthub.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nome: String,
    val username: String,
    val email: String,
    val password: String,
    val foto: String? = null,
    val role: String,
    val createdAt: String? = null,
    val status: String
)