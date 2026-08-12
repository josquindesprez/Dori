package com.dori.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "labels")
data class Label(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorArgb: Int,
    val updatedAt: Long = System.currentTimeMillis(),
    val uuid: String = "",
    val isDeleted: Boolean = false,
    val originDeviceId: String = ""
)
