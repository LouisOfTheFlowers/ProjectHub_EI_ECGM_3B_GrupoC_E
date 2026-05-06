package com.example.projecthub.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tarefa_users")
data class TarefaUserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tarefaId: Int,
    val userId: Int,
    val concluida: Boolean = false
)