package com.example.projecthub.local.dao

import androidx.room.*
import com.example.projecthub.local.entities.TarefaUserEntity

@Dao
interface TarefaUserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarefaUser(tarefaUser: TarefaUserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarefaUsers(tarefaUsers: List<TarefaUserEntity>)

    @Query("SELECT * FROM tarefa_users")
    suspend fun getAllTarefaUsers(): List<TarefaUserEntity>

    @Query("SELECT * FROM tarefa_users WHERE tarefaId = :tarefaId")
    suspend fun getUsersByTarefa(tarefaId: Int): List<TarefaUserEntity>

    @Query("SELECT * FROM tarefa_users WHERE userId = :userId")
    suspend fun getTarefasByUser(userId: Int): List<TarefaUserEntity>

    @Query("UPDATE tarefa_users SET concluida = :concluida WHERE tarefaId = :tarefaId AND userId = :userId")
    suspend fun updateConclusao(tarefaId: Int, userId: Int, concluida: Boolean)

    @Query("DELETE FROM tarefa_users WHERE tarefaId = :tarefaId AND userId = :userId")
    suspend fun deleteTarefaUser(tarefaId: Int, userId: Int)

    @Query("DELETE FROM tarefa_users")
    suspend fun deleteAllTarefaUsers()
}