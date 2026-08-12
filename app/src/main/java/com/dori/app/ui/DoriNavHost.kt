package com.dori.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType

private object Routes {
    const val NOTES = "notes"
    const val DIARY = "diary"
    const val TRACKER = "tracker"
    const val LABELS = "labels"
    const val SUBSTANCES = "substances"
    const val EDITOR = "editor?noteId={noteId}&diaryDate={diaryDate}"

    fun editor(noteId: Long = -1L, diaryDateEpochDay: Long? = null): String =
        "editor?noteId=$noteId&diaryDate=${diaryDateEpochDay ?: -1L}"
}

private data class TopLevelDestination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val topLevelDestinations = listOf(
    TopLevelDestination(Routes.NOTES, "Notes", Icons.Filled.Edit),
    TopLevelDestination(Routes.DIARY, "Diary", Icons.Filled.Book),
    TopLevelDestination(Routes.TRACKER, "Tracker", Icons.Filled.Medication)
)

@Composable
fun DoriApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    val showBottomBar = topLevelDestinations.any { dest ->
        currentRoute?.hierarchy?.any { it.route == dest.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelDestinations.forEach { dest ->
                        val selected = currentRoute?.hierarchy?.any { it.route == dest.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.NOTES,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(Routes.NOTES) {
                NotesScreen(
                    onNoteClick = { id -> navController.navigate(Routes.editor(noteId = id)) },
                    onCreateNote = { navController.navigate(Routes.editor()) },
                    onManageLabels = { navController.navigate(Routes.LABELS) }
                )
            }
            composable(Routes.DIARY) {
                DiaryScreen(
                    onNoteClick = { id -> navController.navigate(Routes.editor(noteId = id)) },
                    onCreateNoteForDate = { date ->
                        navController.navigate(Routes.editor(diaryDateEpochDay = date.toEpochDay()))
                    },
                    onManageLabels = { navController.navigate(Routes.LABELS) }
                )
            }
            composable(Routes.TRACKER) {
                TrackerScreen(
                    onManageSubstances = { navController.navigate(Routes.SUBSTANCES) }
                )
            }
            composable(Routes.LABELS) {
                LabelsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SUBSTANCES) {
                SubstancesScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Routes.EDITOR,
                arguments = listOf(
                    navArgument("noteId") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("diaryDate") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { entry ->
                val noteIdArg = entry.arguments?.getLong("noteId") ?: -1L
                val diaryDateArg = entry.arguments?.getLong("diaryDate") ?: -1L
                EditorScreen(
                    noteId = if (noteIdArg == -1L) null else noteIdArg,
                    initialDiaryDateEpochDay = if (diaryDateArg == -1L) null else diaryDateArg,
                    onBack = { navController.popBackStack() },
                    onManageLabels = { navController.navigate(Routes.LABELS) }
                )
            }
        }
    }
}
