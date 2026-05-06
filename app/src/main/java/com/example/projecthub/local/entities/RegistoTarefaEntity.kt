package com.example.projecthub.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registos_tarefa")
data class RegistoTarefaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tarefaId: Int,
    val userId: Int,
    val data: String,
    val local: String? = null,
    val taxaConclusao: Int,
    val tempoGasto: Float? = null,
    val createdAt: String? = null
)