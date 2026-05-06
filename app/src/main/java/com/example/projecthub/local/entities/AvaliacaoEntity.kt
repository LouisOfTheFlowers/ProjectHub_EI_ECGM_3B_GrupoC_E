package com.example.projecthub.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "avaliacoes")
data class AvaliacaoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val projetoId: Int,
    val userId: Int,
    val classificacao: Int,
    val comentario: String? = null
)