package com.example.projecthub.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "observacoes")
data class ObservacaoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val registoId: Int,
    val texto: String,
    val createdAt: String? = null
)