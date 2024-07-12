package com.example.Test.data.repository

import android.util.Log
import com.example.Test.data.db.PointDao
import com.example.Test.data.model.PointDB

class PointRepository(private val pointDao: PointDao) {

    suspend fun savePoints(points: MutableList<PointDB>) {
        pointDao.deleteAllPoints()
        pointDao.insertPoints(points)
    }

    suspend fun getPoints(): MutableList<PointDB> {
        return pointDao.getAllPoints()
    }
}