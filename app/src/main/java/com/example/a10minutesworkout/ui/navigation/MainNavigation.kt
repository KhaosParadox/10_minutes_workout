package com.example.a10minutesworkout.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.ViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.a10minutesworkout.ui.*

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Home)
    val currentRoute = backStack.lastOrNull() ?: Home

    val showBottomBar = currentRoute !is Workout

    val viewModelStoreProvider = rememberViewModelStoreProvider()

    // 1. Stabilize decorators
    val saveableStateHolderDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
    val viewModelStoreDecorator = remember(viewModelStoreProvider) {
        ViewModelStoreNavEntryDecorator<NavKey>(viewModelStoreProvider)
    }
    val entryDecorators = remember(saveableStateHolderDecorator, viewModelStoreDecorator) {
        listOf(saveableStateHolderDecorator, viewModelStoreDecorator)
    }

    // 2. Define entryProvider (not remembered to ensure it has latest backStack reference if needed)
    val entryProvider: (NavKey) -> NavEntry<NavKey> = { key ->
        when (key) {
            is Home -> NavEntry(key) {
                HomeScreen(onStartWorkout = { backStack.add(Workout) })
            }
            is Workout -> NavEntry(key) {
                val viewModel: WorkoutViewModel = viewModel()
                WorkoutScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            is Calendar -> NavEntry(key) {
                CalendarScreen()
            }
            is Settings -> NavEntry(key) {
                SettingsScreen()
            }
            else -> NavEntry(key) { Text("Unknown") }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        val navKey = route as NavKey
                        if (backStack.size != 1 || backStack.first() != navKey) {
                            backStack.clear()
                            backStack.add(navKey)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            popTransitionSpec = { fadeIn() togetherWith fadeOut() },
            predictivePopTransitionSpec = { fadeIn() togetherWith fadeOut() },
            entryDecorators = entryDecorators,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider
        )
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: Any,
    onNavigate: (Any) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute is Home,
            onClick = { onNavigate(Home) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Accueil") },
            label = { Text("Accueil") }
        )
        NavigationBarItem(
            selected = currentRoute is Calendar,
            onClick = { onNavigate(Calendar) },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendrier") },
            label = { Text("Calendrier") }
        )
        NavigationBarItem(
            selected = currentRoute is Settings,
            onClick = { onNavigate(Settings) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Paramètres") },
            label = { Text("Paramètres") }
        )
    }
}
