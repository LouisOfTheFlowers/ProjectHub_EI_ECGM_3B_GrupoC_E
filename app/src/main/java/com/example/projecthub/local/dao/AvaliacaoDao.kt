package com.example.projecthub.local.dao

import androidx.room.*
import com.example.projecthub.local.entities.AvaliacaoEntity

@Dao
interface AvaliacaoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvaliacao(avaliacao: AvaliacaoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvaliacoes(avaliacoes: List<AvaliacaoEntity>)

    @Query("SELECT * FROM avaliacoes")
    suspend fun getAllAvaliacoes(): List<AvaliacaoEntity>

    @Query("SELECT * FROM avaliacoes WHERE id = :id LIMIT 1")
    suspend fun getAvaliacaoById(id: Int): AvaliacaoEntity?

    @Query("SELECT * FROM avaliacoes WHERE projetoId = :projetoId")
    suspend fun getAvaliacoesByProjeto(projetoId: Int): List<AvaliacaoEntity>

    @Query("SELECT * FROM avaliacoes WHERE userId = :userId")
    suspend fun getAvaliacoesByUser(userId: Int): List<AvaliacaoEntity>

    @Update
    suspend fun updateAvaliacao(avaliacao: AvaliacaoEntity)

    @Delete
    suspend fun deleteAvaliacao(avaliacao: AvaliacaoEntity)

    @Query("DELETE FROM avaliacoes")
    suspend fun deleteAllAvaliacoes()
}