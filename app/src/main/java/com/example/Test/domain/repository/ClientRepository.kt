package com.example.Test.domain.repository

import com.example.Test.UI.client.ClientFragment

interface ClientRepository {
    suspend fun clientConnection(ip: String, port: Int, periodically: Int)
    suspend fun clientDisconnect()
    fun isConnected() : Boolean
    suspend fun sendSwipeDataToServer()
}