package com.example.Test.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.example.Test.UI.client.ClientFragment
import com.example.Test.data.model.Point
import com.example.Test.data.model.PointDB
import com.example.Test.domain.repository.ServerRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.ktor.http.ContentType
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.*
import io.ktor.server.netty.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration

class ServerRepositoryImpl() : ServerRepository {

    private var server: NettyApplicationEngine? = null
    private var serverJob: Job? = null
    private var points = mutableListOf<Point>()

    var onGetNewPoints: ((points: MutableList<Point>) -> Unit)? = null

    override suspend fun startServer(port: Int) {
        points.clear()
        serverJob = CoroutineScope(Dispatchers.IO).launch {
            server = embeddedServer(Netty, port = port) {
                install(WebSockets) {
                    pingPeriod = Duration.ofSeconds(15)
                    timeout = Duration.ofSeconds(15)
                    maxFrameSize = Long.MAX_VALUE
                    masking = false
                }
                routing {
                    webSocket("/") {
                        while (true) {
                            try {
                                for (frame in incoming) {
                                    if (frame is Frame.Text) {
                                        val receivedText = frame.readText()
                                        val gson = Gson()
                                        val pointType = object : TypeToken<MutableList<Point>>() {}.type
                                        val pointList: List<Point> = gson.fromJson(receivedText, pointType)
                                        points += pointList.toMutableList()
                                        onGetNewPoints?.invoke(pointList.toMutableList())
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }.start(wait = true)
        }
    }

    fun getPointsDb(): MutableList<PointDB> {
        val pointsDb = mutableListOf<PointDB>()
        for (i in 0 until points.size) {
            var pointDb = PointDB(
                i,
                points[i].x,
                points[i].y,
                points[i].size,
                points[i].pressure,
                points[i].isEnd
            )
            pointsDb.add(pointDb)
        }
        return pointsDb
    }

    override suspend fun stopServer() {
        serverJob?.cancel()
        CoroutineScope(Dispatchers.IO).launch {
            server?.stop(1000, 1000)
        }
    }
}