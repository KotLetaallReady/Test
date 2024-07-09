package com.example.Test.domain.repository

interface ServerRepository {
    suspend fun startServer(port: Int)
    suspend fun stopServer()
}