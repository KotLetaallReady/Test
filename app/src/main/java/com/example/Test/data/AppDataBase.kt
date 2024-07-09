package com.example.Test.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.Test.data.db.PointDao
import com.example.Test.data.model.PointDB

@Database(entities = [PointDB::class], version = 1)
abstract class AppDataBase : RoomDatabase() {
    abstract fun pointDao(): PointDao

    companion object {
        @Volatile private var INSTANCE: AppDataBase? = null

        fun getDatabase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}