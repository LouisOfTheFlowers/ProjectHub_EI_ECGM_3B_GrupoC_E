package com.example.projecthub.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projetos")
data class ProjetoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nome: String,
    val descricao: String? = null,
    val dataInicio: String? = null,
    val dataFim: String? = null,
    val status: String = "PENDENTE",
    val gestorId: Int,
    val createdAt: String? = null
)