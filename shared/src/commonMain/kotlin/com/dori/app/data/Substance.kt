package com.dori.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "substances")
data class Substance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val unit: String,
    val colorArgb: Int,
    val updatedAt: Long = System.currentTimeMillis(),
    val uuid: String = "",
    val isDeleted: Boolean = false,
    val originDeviceId: String = ""
)
