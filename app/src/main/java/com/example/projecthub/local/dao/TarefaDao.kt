package com.example.projecthub.local.dao

import androidx.room.*
import com.example.projecthub.local.entities.TarefaEntity

@Dao
interface TarefaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarefa(tarefa: TarefaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarefas(tarefas: List<TarefaEntity>)

    @Query("SELECT * FROM tarefas")
    suspend fun getAllTarefas(): List<TarefaEntity>

    @Query("SELECT * FROM tarefas WHERE id = :id LIMIT 1")
    suspend fun getTarefaById(id: Int): TarefaEntity?

    @Query("SELECT * FROM tarefas WHERE projetoId = :projetoId")
    suspend fun getTarefasByProjeto(projetoId: Int): List<TarefaEntity>

    @Query("SELECT * FROM tarefas WHERE status = :status")
    suspend fun getTarefasByStatus(status: String): List<TarefaEntity>

    @Update
    suspend fun updateTarefa(tarefa: TarefaEntity)

    @Delete
    suspend fun deleteTarefa(tarefa: TarefaEntity)

    @Query("DELETE FROM tarefas")
    suspend fun deleteAllTarefas()
}