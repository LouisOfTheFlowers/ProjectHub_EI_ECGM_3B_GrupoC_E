package com.example.projecthub.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tarefas")
data class TarefaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val descricao: String? = null,
    val projetoId: Int,
    val status: String = "PENDENTE",
    val dataInicio: String? = null,
    val dataFim: String? = null
)