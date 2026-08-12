package com.dori.app.sync

import com.dori.app.data.Label
import com.dori.app.data.Note
import com.dori.app.data.Substance
import com.dori.app.data.SubstanceEntry
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class NoteDto(
    val uuid: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
    val diaryDateEpochDay: Long?,
    val labelUuid: String?,
    val isDeleted: Boolean,
    val originDeviceId: String
)

@Serializable
data class LabelDto(
    val uuid: String,
    val name: String,
    val colorArgb: Int,
    val updatedAt: Long,
    val isDeleted: Boolean,
    val originDeviceId: String
)

@Serializable
data class SubstanceDto(
    val uuid: String,
    val name: String,
    val unit: String,
    val colorArgb: Int,
    val updatedAt: Long,
    val isDeleted: Boolean,
    val originDeviceId: String
)

@Serializable
data class SubstanceEntryDto(
    val uuid: String,
    val substanceUuid: String,
    val quantity: Double,
    val price: Double,
    val source: String,
    val comment: String,
    val occurredAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean,
    val originDeviceId: String
)

@Serializable
data class SyncRequest(
    val fromDeviceId: String,
    val fromDeviceName: String,
    val sinceWatermark: Long,
    val labels: List<LabelDto>,
    val notes: List<NoteDto>,
    val substances: List<SubstanceDto> = emptyList(),
    val entries: List<SubstanceEntryDto> = emptyList()
)

@Serializable
data class SyncResponse(
    val fromDeviceId: String,
    val fromDeviceName: String,
    val labels: List<LabelDto>,
    val notes: List<NoteDto>,
    val serverWatermarkNow: Long,
    val substances: List<SubstanceDto> = emptyList(),
    val entries: List<SubstanceEntryDto> = emptyList()
)

@Serializable
data class DiscoveryBeacon(
    val deviceId: String,
    val deviceName: String,
    val syncPort: Int
)

fun Note.toDto() = NoteDto(
    uuid = uuid,
    title = title,
    body = body,
    createdAt = createdAt,
    updatedAt = updatedAt,
    diaryDateEpochDay = diaryDate?.toEpochDay(),
    labelUuid = labelUuid,
    isDeleted = isDeleted,
    originDeviceId = originDeviceId
)

fun NoteDto.toNote() = Note(
    title = title,
    body = body,
    createdAt = createdAt,
    updatedAt = updatedAt,
    diaryDate = diaryDateEpochDay?.let(LocalDate::ofEpochDay),
    labelId = null,
    uuid = uuid,
    labelUuid = labelUuid,
    isDeleted = isDeleted,
    originDeviceId = originDeviceId
)

fun Label.toDto() = LabelDto(
    uuid = uuid,
    name = name,
    colorArgb = colorArgb,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    originDeviceId = originDeviceId
)

fun LabelDto.toLabel() = Label(
    name = name,
    colorArgb = colorArgb,
    updatedAt = updatedAt,
    uuid = uuid,
    isDeleted = isDeleted,
    originDeviceId = originDeviceId
)

fun Substance.toDto() = SubstanceDto(
    uuid = uuid,
    name = name,
    unit = unit,
    colorArgb = colorArgb,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    originDeviceId = originDeviceId
)

fun SubstanceDto.toSubstance() = Substance(
    name = name,
    unit = unit,
    colorArgb = colorArgb,
    updatedAt = updatedAt,
    uuid = uuid,
    isDeleted = isDeleted,
    originDeviceId = originDeviceId
)

fun SubstanceEntry.toDto() = SubstanceEntryDto(
    uuid = uuid,
    substanceUuid = substanceUuid,
    quantity = quantity,
    price = price,
    source = source,
    comment = comment,
    occurredAt = occurredAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    originDeviceId = originDeviceId
)

fun SubstanceEntryDto.toEntry() = SubstanceEntry(
    substanceId = 0,
    quantity = quantity,
    price = price,
    source = source,
    comment = comment,
    occurredAt = occurredAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    uuid = uuid,
    substanceUuid = substanceUuid,
    isDeleted = isDeleted,
    originDeviceId = originDeviceId
)
