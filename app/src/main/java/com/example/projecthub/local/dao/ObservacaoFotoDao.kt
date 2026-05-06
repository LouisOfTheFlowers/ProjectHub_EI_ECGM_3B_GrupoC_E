package com.example.projecthub.local.dao

import androidx.room.*
import com.example.projecthub.local.entities.ObservacaoFotoEntity

@Dao
interface ObservacaoFotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoto(foto: ObservacaoFotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFotos(fotos: List<ObservacaoFotoEntity>)

    @Query("SELECT * FROM observacao_fotos")
    suspend fun getAllFotos(): List<ObservacaoFotoEntity>

    @Query("SELECT * FROM observacao_fotos WHERE observacaoId = :observacaoId")
    suspend fun getFotosByObservacao(observacaoId: Int): List<ObservacaoFotoEntity>

    @Delete
    suspend fun deleteFoto(foto: ObservacaoFotoEntity)

    @Query("DELETE FROM observacao_fotos")
    suspend fun deleteAllFotos()
}