package com.example.projecthub.local.dao

import androidx.room.*
import com.example.projecthub.local.entities.RegistoTarefaEntity

@Dao
interface RegistoTarefaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegisto(registo: RegistoTarefaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistos(registos: List<RegistoTarefaEntity>)

    @Query("SELECT * FROM registos_tarefa")
    suspend fun getAllRegistos(): List<RegistoTarefaEntity>

    @Query("SELECT * FROM registos_tarefa WHERE id = :id LIMIT 1")
    suspend fun getRegistoById(id: Int): RegistoTarefaEntity?

    @Query("SELECT * FROM registos_tarefa WHERE tarefaId = :tarefaId")
    suspend fun getRegistosByTarefa(tarefaId: Int): List<RegistoTarefaEntity>

    @Query("SELECT * FROM registos_tarefa WHERE userId = :userId")
    suspend fun getRegistosByUser(userId: Int): List<RegistoTarefaEntity>

    @Update
    suspend fun updateRegisto(registo: RegistoTarefaEntity)

    @Delete
    suspend fun deleteRegisto(registo: RegistoTarefaEntity)

    @Query("DELETE FROM registos_tarefa")
    suspend fun deleteAllRegistos()
}
