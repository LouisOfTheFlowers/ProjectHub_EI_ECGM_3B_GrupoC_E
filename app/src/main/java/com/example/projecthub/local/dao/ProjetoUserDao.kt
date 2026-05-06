package com.example.projecthub.local.dao

import androidx.room.*
import com.example.projecthub.local.entities.ProjetoUserEntity

@Dao
interface ProjetoUserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjetoUser(projetoUser: ProjetoUserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjetoUsers(projetoUsers: List<ProjetoUserEntity>)

    @Query("SELECT * FROM projeto_users")
    suspend fun getAllProjetoUsers(): List<ProjetoUserEntity>

    @Query("SELECT * FROM projeto_users WHERE projetoId = :projetoId")
    suspend fun getUsersByProjeto(projetoId: Int): List<ProjetoUserEntity>

    @Query("SELECT * FROM projeto_users WHERE userId = :userId")
    suspend fun getProjetosByUser(userId: Int): List<ProjetoUserEntity>

    @Query("DELETE FROM projeto_users WHERE projetoId = :projetoId AND userId = :userId")
    suspend fun deleteProjetoUser(projetoId: Int, userId: Int)

    @Query("DELETE FROM projeto_users")
    suspend fun deleteAllProjetoUsers()
}