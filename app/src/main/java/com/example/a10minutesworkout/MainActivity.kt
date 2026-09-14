package com.example.a10minutesworkout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.a10minutesworkout.ui.navigation.MainNavigation
import com.example.a10minutesworkout.ui.theme.WorkoutTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkoutTheme {
                MainNavigation()
            }
        }
    }
}
