package com.example.projecthub.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projeto_users")
data class ProjetoUserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val projetoId: Int,
    val userId: Int
)