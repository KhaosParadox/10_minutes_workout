package com.example.a10minutesworkout.ui

import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.a10minutesworkout.data.ExerciseData
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(viewModel: WorkoutViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    // Keep screen on logic
    DisposableEffect(Unit) {
        val activity = context as? android.app.Activity
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { _ ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header: Progress and Phase
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${uiState.currentExerciseIndex + 1} / ${uiState.totalExercises}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PhaseText(uiState)
                }

                // Middle: GIF Display and Timer
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        val gifName = if (uiState.phase == WorkoutPhase.EFFORT) {
                            uiState.currentExercise?.gifResourceName
                        } else {
                            val nextIndex = if (uiState.phase == WorkoutPhase.PREPARATION) {
                                uiState.currentExerciseIndex
                            } else {
                                uiState.currentExerciseIndex + 1
                            }
                            uiState.exercises.getOrNull(nextIndex)?.gifResourceName
                        }
                        
                        if (gifName != null) {
                            val resId = context.resources.getIdentifier(
                                gifName,
                                "drawable",
                                context.packageName
                            )
                            if (resId != 0) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(resId)
                                        .build(),
                                    contentDescription = uiState.currentExercise?.name,
                                    imageLoader = imageLoader,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    TimerDisplay(uiState.timeLeftMillis)
                }

                // Bottom: Controls
                WorkoutControls(
                    isPaused = uiState.isPaused,
                    onTogglePause = viewModel::togglePause,
                    onSkipForward = viewModel::skipForward,
                    onSkipBackward = viewModel::skipBackward
                )
            }

            // Top Buttons overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.dismissCompletionDialog(onBack) },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour"
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleMusicMute() },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (uiState.isMusicMuted) Icons.Default.MusicOff else Icons.Default.MusicNote,
                        contentDescription = if (uiState.isMusicMuted) "Activer musique" else "Couper musique"
                    )
                }
            }
        }
    }

    if (uiState.isCompleted) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCompletionDialog(onBack) },
            title = { Text("Félicitations !") },
            text = { Text("Vous avez terminé votre séance de 10 minutes. Votre progression a été enregistrée.") },
            confirmButton = {
                Button(onClick = { viewModel.dismissCompletionDialog(onBack) }) {
                    Text("Retour à l'accueil")
                }
            }
        )
    }
}

@Composable
fun PhaseText(uiState: WorkoutUiState) {
    val text = when (uiState.phase) {
        WorkoutPhase.PREPARATION -> "PREPARATION"
        WorkoutPhase.EFFORT -> "GO: ${uiState.currentExercise?.name?.uppercase(Locale.ROOT) ?: ""}"
        WorkoutPhase.REST -> "REST (Next: ${uiState.exercises.getOrNull(uiState.currentExerciseIndex + 1)?.name ?: ""})"
        WorkoutPhase.COMPLETED -> "COMPLETED!"
    }
    
    val color = when (uiState.phase) {
        WorkoutPhase.EFFORT -> MaterialTheme.colorScheme.primary
        WorkoutPhase.REST -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.secondary
    }

    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        ),
        color = color,
        textAlign = TextAlign.Center
    )
}

@Composable
fun TimerDisplay(millis: Long) {
    val totalSeconds = (millis / 1000).toInt()
    val secondsDisplay = String.format(Locale.ROOT, "%02d", totalSeconds)
    
    Text(
        text = secondsDisplay,
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = 120.sp,
            fontWeight = FontWeight.Black
        ),
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun WorkoutControls(
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onSkipBackward,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                modifier = Modifier.size(32.dp)
            )
        }

        FilledIconButton(
            onClick = onTogglePause,
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(24.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (isPaused) "Play" else "Pause",
                modifier = Modifier.size(40.dp)
            )
        }

        IconButton(
            onClick = onSkipForward,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
