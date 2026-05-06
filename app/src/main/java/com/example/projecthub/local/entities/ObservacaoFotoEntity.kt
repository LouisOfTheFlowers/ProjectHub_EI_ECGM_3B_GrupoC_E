package com.example.projecthub.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "observacao_fotos")
data class ObservacaoFotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val observacaoId: Int,
    val fotoUrl: String
)