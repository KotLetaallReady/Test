package com.example.Test.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "points")
data class PointDB(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @SerializedName("x") val x: Float,
    @SerializedName("y") val y: Float,
    @SerializedName("size") val size: Float,
    @SerializedName("pressure") val pressure: Float,
    @SerializedName("isEnd") val isEnd: Boolean = false
)