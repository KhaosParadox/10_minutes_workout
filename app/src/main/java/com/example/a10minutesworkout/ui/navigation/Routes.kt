package com.example.a10minutesworkout.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
object Home : NavKey

@Serializable
data class Workout(val gentle: Boolean = false) : NavKey

@Serializable
object Calendar : NavKey

@Serializable
object Settings : NavKey
