package com.dori.desktop

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.dori.app.data.NoteRepository
import com.dori.app.data.createDesktopDatabase
import com.dori.app.sync.SyncEngine
import org.jetbrains.skia.Image

fun main() {
    val database = createDesktopDatabase()
    val repository = NoteRepository(
        noteDao = database.noteDao(),
        labelDao = database.labelDao(),
        deviceIdentityDao = database.deviceIdentityDao(),
        syncPeerStateDao = database.syncPeerStateDao(),
        substanceDao = database.substanceDao(),
        substanceEntryDao = database.substanceEntryDao(),
        deviceName = "Windows"
    )
    SyncEngine(repository).start()

    val appIcon = Thread.currentThread().contextClassLoader
        .getResourceAsStream("icon.png")
        ?.use { it.readBytes() }
        ?.let { bytes -> BitmapPainter(Image.makeFromEncoded(bytes).toComposeImageBitmap()) }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Dori",
            icon = appIcon,
            state = WindowState(
                position = WindowPosition(alignment = androidx.compose.ui.Alignment.Center),
                width = 1000.dp,
                height = 700.dp
            )
        ) {
            DoriDesktopApp(repository)
        }
    }
}
