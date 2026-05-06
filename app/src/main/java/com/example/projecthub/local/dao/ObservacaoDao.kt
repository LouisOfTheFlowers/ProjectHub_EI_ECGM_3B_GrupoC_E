package com.example.projecthub.local.dao

import androidx.room.*
import com.example.projecthub.local.entities.ObservacaoEntity

@Dao
interface ObservacaoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservacao(observacao: ObservacaoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservacoes(observacoes: List<ObservacaoEntity>)

    @Query("SELECT * FROM observacoes")
    suspend fun getAllObservacoes(): List<ObservacaoEntity>

    @Query("SELECT * FROM observacoes WHERE id = :id LIMIT 1")
    suspend fun getObservacaoById(id: Int): ObservacaoEntity?

    @Query("SELECT * FROM observacoes WHERE registoId = :registoId")
    suspend fun getObservacoesByRegisto(registoId: Int): List<ObservacaoEntity>

    @Update
    suspend fun updateObservacao(observacao: ObservacaoEntity)

    @Delete
    suspend fun deleteObservacao(observacao: ObservacaoEntity)

    @Query("DELETE FROM observacoes")
    suspend fun deleteAllObservacoes()
}