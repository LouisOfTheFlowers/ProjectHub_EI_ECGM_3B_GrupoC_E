package com.example.projecthub.local.dao

import androidx.room.*
import com.example.projecthub.local.entities.ProjetoEntity

@Dao
interface ProjetoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjeto(projeto: ProjetoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjetos(projetos: List<ProjetoEntity>)

    @Query("SELECT * FROM projetos")
    suspend fun getAllProjetos(): List<ProjetoEntity>

    @Query("SELECT * FROM projetos WHERE id = :id LIMIT 1")
    suspend fun getProjetoById(id: Int): ProjetoEntity?

    @Query("SELECT * FROM projetos WHERE gestorId = :gestorId")
    suspend fun getProjetosByGestor(gestorId: Int): List<ProjetoEntity>

    @Query("SELECT * FROM projetos WHERE status = :status")
    suspend fun getProjetosByStatus(status: String): List<ProjetoEntity>

    @Update
    suspend fun updateProjeto(projeto: ProjetoEntity)

    @Delete
    suspend fun deleteProjeto(projeto: ProjetoEntity)

    @Query("DELETE FROM projetos")
    suspend fun deleteAllProjetos()
}