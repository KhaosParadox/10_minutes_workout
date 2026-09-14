package com.example.a10minutesworkout.ui

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.decode.GifDecoder
import android.os.Build
import com.example.a10minutesworkout.model.*

@Composable
fun WorkoutScreen(viewModel: WorkoutViewModel, gentle: Boolean = false, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var confirmExit by rememberSaveable { mutableStateOf(false) }
    var feedback by rememberSaveable { mutableStateOf("ADAPTED") }
    var showEasier by rememberSaveable { mutableStateOf(false) }
    val loader = remember { ImageLoader.Builder(context).components {
        if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
    }.build() }
    LaunchedEffect(Unit) { viewModel.start(gentle) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) viewModel.onForeground(false)
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onForeground(true)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer); loader.shutdown() }
    }
    DisposableEffect(state.progress.paused, state.completed) {
        val window = (context as? Activity)?.window
        if (!state.progress.paused && !state.completed) window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    BackHandler {
        if (!state.saving) {
            if (state.saved || state.plan == null) onBack()
            else if (!state.completed) { viewModel.pause(); confirmExit = true }
        }
    }
    val plan = state.plan
    val step = plan?.steps?.getOrNull(state.progress.index)
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { if (plan == null) onBack() else { viewModel.pause(); confirmExit = true } }, enabled = !state.saving) { Text("Quitter") }
                TextButton(onClick = viewModel::toggleMusicMute) { Text(if (state.muted) "Musique : non" else "Musique : oui") }
            }
            if (plan == null) {
                Text(state.error ?: "Préparation de ta séance…")
            } else if (step != null) {
                Text(plan.title, style = MaterialTheme.typography.titleMedium)
                Text(when (step.kind) {
                    StepKind.PREPARE -> "PRÉPARATION"
                    StepKind.WARMUP -> "MISE EN MOUVEMENT"
                    StepKind.WORK -> "À TON RYTHME"
                    StepKind.REST -> "RÉCUPÉRATION"
                    StepKind.COOLDOWN -> "RETOUR AU CALME"
                }, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(step.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(formatDuration(((state.progress.remainingMillis + 999) / 1000).toInt()), style = MaterialTheme.typography.displayLarge)
                Text(if (state.progress.paused) "En pause · reprends quand tu es prêt" else "Encore ${formatDuration(((state.remainingTotalMillis + 999) / 1000).toInt())} au total")
                LinearProgressIndicator(progress = { state.progress.index.toFloat() / plan.steps.size }, modifier = Modifier.fillMaxWidth())
                Text(step.cue, style = MaterialTheme.typography.bodyLarge)
                if (step.kind == StepKind.WORK) {
                    TextButton(onClick = { showEasier = !showEasier }) { Text("Option plus douce") }
                    if (showEasier) Text(step.easier)
                }
                step.gif?.let { name ->
                    val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
                    if (resId != 0) AsyncImage(model = resId, contentDescription = step.name, imageLoader = loader,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 180.dp))
                }
                plan.steps.drop(state.progress.index + 1).firstOrNull { it.kind != StepKind.REST }?.let { Text("Ensuite : ${it.name}") }
                state.audioError?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        }
        if (step != null) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = viewModel::togglePause, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) { Text(if (state.progress.paused) "Reprendre" else "Pause") }
                OutlinedButton(onClick = viewModel::skip, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) { Text("Passer") }
            }
        }
    }
    if (state.completed || confirmExit) {
        AlertDialog(
            onDismissRequest = { if (!state.completed && !state.saving) confirmExit = false },
            title = { Text(if (state.completed) "Bien joué !" else "Terminer ici ?") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${formatDuration((state.progress.elapsedMillis / 1000).toInt())} réalisées · ${formatDuration((state.progress.activeMillis / 1000).toInt())} dans les exercices, hors échauffement et repos.")
                    if (!state.completed || state.progress.skippedWork > 0) Text("Séance partielle : ton temps sera conservé sans augmenter la difficulté ni avancer le parcours.")
                    Text("Comment c’était ?")
                    listOf("EASY" to "Facile", "ADAPTED" to "Adapté", "HARD" to "Difficile").forEach { (value, label) ->
                        FilterChip(selected = feedback == value, onClick = { feedback = value }, label = { Text(label) }, enabled = !state.saving)
                    }
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = { Button(enabled = !state.saving, onClick = { viewModel.save(feedback, onBack) }) { Text(if (state.saving) "Enregistrement…" else "Enregistrer et rentrer") } },
            dismissButton = { if (!state.completed) TextButton(enabled = !state.saving, onClick = { confirmExit = false }) { Text("Continuer la séance") } }
        )
    }
}
