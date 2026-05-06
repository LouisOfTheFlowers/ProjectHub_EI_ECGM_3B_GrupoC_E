package com.example.projecthub.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.projecthub.local.dao.SyncQueueDao
import com.example.projecthub.local.dao.UserDao
import com.example.projecthub.local.entities.SyncQueueEntity
import com.example.projecthub.local.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun syncQueueDao(): SyncQueueDao
}