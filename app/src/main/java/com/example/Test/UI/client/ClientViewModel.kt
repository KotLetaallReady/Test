package com.example.Test.UI.client

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.Test.data.model.Point
import com.example.Test.data.repository.ClientRepositoryImpl
import io.ktor.client.HttpClient

import io.ktor.client.engine.android.*
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.properties.Delegates

class ClientViewModel : ViewModel() {

    private val clientRepository = ClientRepositoryImpl()

    private var serverIp = "192.168.31.207"
    private var serverPort = 8080
    private var serverPeriodically = 100

    fun setConf(ip: String, port: Int, periodically: Int){
        serverIp = ip
        serverPort = port
        serverPeriodically = periodically
    }

    fun connectWebSocket(
        ip: String = serverIp,
        port: Int = serverPort,
        periodically: Int = serverPeriodically
    ) {
        Log.d("ClientViewModel", "Connecting to WebSocket: IP = $ip, Port = $port, Periodically = $periodically")
        viewModelScope.launch(Dispatchers.IO) {
            clientRepository.clientConnection(ip, port, periodically)
        }
    }

    // Новый метод для отправки точки касания
    fun sendPoint(point: Point) {
        clientRepository.addPointToSend(point)
    }

    fun disconnectWebSocket() {
        viewModelScope.launch(Dispatchers.IO) {
            clientRepository.clientDisconnect()
        }
    }
}