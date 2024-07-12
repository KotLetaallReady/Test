package com.example.Test.data.repository

import kotlinx.coroutines.CoroutineScope
import android.util.Log
import com.example.Test.UI.client.ClientFragment
import com.example.Test.data.model.Point
import com.example.Test.domain.repository.ClientRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

class ClientRepositoryImpl : ClientRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var isConnected: Boolean = false
    private var periodically: Int = 1000
    private var points = mutableListOf<Point>()
    private var job = Job()
    private var coroutineScope = CoroutineScope(Dispatchers.IO + job)

    override suspend fun clientConnection(ip: String, port: Int, periodically: Int) {
        val url = "ws://$ip:$port/"
        this.periodically = periodically
        val request = Request.Builder().url(url).build()
        val listener = SocketListener()
        try {
            Log.e("break", "Connected to $url")
            webSocket = client.newWebSocket(request, listener)
        } catch (e: Exception) {
        }
    }

    override suspend fun clientDisconnect() {
        webSocket?.close(1000, "Client is closing the connection")
        webSocket = null
        isConnected = false
        job.cancel()
        job = Job()
        coroutineScope = CoroutineScope(Dispatchers.IO + job)
    }

    private fun pointsToJson(points: MutableList<Point>): String {
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(points)
    }

    override suspend fun sendSwipeDataToServer() {
        while (isConnected) {
            if (points.isNotEmpty()) {
                val gson = pointsToJson(points.toMutableList())
                points.clear()
                webSocket?.send(gson)
            }
            delay(periodically.toLong())
        }
    }

    fun addPointToSend(point: Point) {
        points.add(point)
    }

    private inner class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            Log.e("break", " Connected")
            isConnected = true
            coroutineScope.launch {
                try {
                    sendSwipeDataToServer()
                }catch (e :Exception){
                }
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            super.onMessage(webSocket, bytes)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            isConnected = false
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            isConnected = false
        }
    }

    override fun isConnected(): Boolean {
        return isConnected
    }
}
