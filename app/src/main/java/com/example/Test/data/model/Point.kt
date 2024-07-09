package com.example.Test.data.model

import com.google.gson.annotations.SerializedName

data class Point(
    @SerializedName("x") val x: Float,
    @SerializedName("y") val y: Float,
    @SerializedName("size") val size: Float,
    @SerializedName("pressure") val pressure: Float,
    @SerializedName("isEnd") val isEnd: Boolean = false
)
