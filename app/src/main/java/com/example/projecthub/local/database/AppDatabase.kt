package com.example.projecthub.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.projecthub.local.dao.*
import com.example.projecthub.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        ProjetoEntity::class,
        ProjetoUserEntity::class,
        TarefaEntity::class,
        TarefaUserEntity::class,
        RegistoTarefaEntity::class,
        ObservacaoEntity::class,
        ObservacaoFotoEntity::class,
        AvaliacaoEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun projetoDao(): ProjetoDao
    abstract fun projetoUserDao(): ProjetoUserDao

    abstract fun tarefaDao(): TarefaDao
    abstract fun tarefaUserDao(): TarefaUserDao

    abstract fun registoTarefaDao(): RegistoTarefaDao

    abstract fun observacaoDao(): ObservacaoDao
    abstract fun observacaoFotoDao(): ObservacaoFotoDao

    abstract fun avaliacaoDao(): AvaliacaoDao

    abstract fun syncQueueDao(): SyncQueueDao
}