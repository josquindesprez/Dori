package com.dori.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "substance_entries",
    indices = [Index("occurredAt"), Index("substanceId")]
)
data class SubstanceEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val substanceId: Long,
    val quantity: Double,
    val price: Double,
    val source: String = "",
    val comment: String = "",
    // When the substance was actually used - user-editable, defaults to now
    // at creation but is distinct from createdAt/updatedAt below.
    val occurredAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // Stable cross-device identity, same rationale as Note.uuid.
    val uuid: String = "",
    val substanceUuid: String = "",
    val isDeleted: Boolean = false,
    val originDeviceId: String = ""
)
