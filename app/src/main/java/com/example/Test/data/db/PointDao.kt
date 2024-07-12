package com.example.Test.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.Test.data.model.PointDB

@Dao
interface PointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoints(points: MutableList<PointDB>)

    @Query("DELETE FROM points")
    suspend fun deleteAllPoints()

    @Query("SELECT * FROM points")
    suspend fun getAllPoints(): MutableList<PointDB>
}