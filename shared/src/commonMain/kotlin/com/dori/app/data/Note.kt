package com.dori.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val body: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val diaryDate: LocalDate? = null,
    val labelId: Long? = null,
    // Stable cross-device identity - never regenerated once assigned, unlike
    // `id` which is only meaningful within this device's own database.
    val uuid: String = "",
    val labelUuid: String? = null,
    val isDeleted: Boolean = false,
    val originDeviceId: String = ""
) {
    val isEmpty: Boolean
        get() = title.isBlank() && body.isBlank()
}
