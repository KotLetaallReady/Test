package com.example.Test.UI.server

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.Test.data.AppDataBase
import com.example.Test.data.db.PointDao
import com.example.Test.data.model.Point
import com.example.Test.data.model.PointDB
import com.example.Test.data.repository.PointRepository
import com.example.Test.data.repository.ServerRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServerViewModel : ViewModel() {
    val serverRepository = ServerRepositoryImpl()

    private lateinit var pointDao: PointDao
    private lateinit var pointRepository: PointRepository

    private val _editTextPort = MutableLiveData<Int>()
    val editTextPort: LiveData<Int> = _editTextPort

    private var pointsDb = mutableListOf<PointDB>()

    private val _points = MutableLiveData<MutableList<Point>>()
    val points: LiveData<MutableList<Point>> = _points

    fun serverOn(port: Int = editTextPort.value!!) {
        viewModelScope.launch(Dispatchers.IO) {
            serverRepository.startServer(port)
        }
    }

    fun savePoint(context: Context) {
        pointDao = AppDataBase.getDatabase(context).pointDao()
        pointRepository = PointRepository(pointDao)
        viewModelScope.launch(Dispatchers.IO) {
            pointRepository.savePoints(serverRepository.getPointsDb())
        }
    }

    fun getPoint(context: Context) {
        pointDao = AppDataBase.getDatabase(context).pointDao()
        pointRepository = PointRepository(pointDao)
        viewModelScope.launch(Dispatchers.IO) {
            val pointsDb = pointRepository.getPoints()
            dbToPoint(pointsDb)
        }
    }

    private fun dbToPoint(pointsDb: MutableList<PointDB>) {
        val pointsList = mutableListOf<Point>()

        for (pointDb in pointsDb) {
            val point = Point(
                pointDb.x,
                pointDb.y,
                pointDb.size,
                pointDb.pressure,
                pointDb.isEnd
            )
            pointsList.add(point)
        }

        _points.postValue(pointsList)
    }

    fun serverOff() {
        viewModelScope.launch(Dispatchers.IO) {
            serverRepository.stopServer()
        }
        pointsDb = serverRepository.getPointsDb() ?: mutableListOf()
    }

    fun setPort(text: String) {
        _editTextPort.value = text.toInt()
    }

    fun deletePoints(){
        _points.value?.clear()
    }
}