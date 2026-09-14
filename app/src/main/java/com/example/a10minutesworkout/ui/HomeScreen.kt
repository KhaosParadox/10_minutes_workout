package com.example.a10minutesworkout.ui

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a10minutesworkout.data.*
import com.example.a10minutesworkout.model.*
import kotlinx.coroutines.flow.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    val sessions = WorkoutDatabase.getDatabase(application).workoutDao().getAllSessions()
        .map<List<WorkoutSession>, List<WorkoutSession>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

@Composable
fun HomeScreen(onStartWorkout: (Boolean) -> Unit, viewModel: HomeViewModel = viewModel()) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val plan = sessions?.let { PersonalProgram.recommended(it) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Ton rendez-vous forme", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Un peu chaque jour. De la force, du souffle et de la souplesse.", style = MaterialTheme.typography.bodyLarge)
        if (plan == null) { CircularProgressIndicator(); return@Column }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("AUJOURD’HUI", style = MaterialTheme.typography.labelLarge)
                Text(plan.title, style = MaterialTheme.typography.headlineSmall)
                Text("${formatDuration(plan.seconds)} min · ${if (plan.isStrength) "palier ${plan.level + 1}/3" else "rythme confortable"}")
                Text(plan.equipment)
                Text(plan.description)
                Button(onClick = { onStartWorkout(false) }, Modifier.fillMaxWidth()) { Text("Commencer ma séance") }
            }
        }
        OutlinedButton(onClick = { onStartWorkout(true) }, Modifier.fillMaxWidth()) { Text("Aujourd’hui, je préfère une séance douce") }
        Text("Le parcours", style = MaterialTheme.typography.titleLarge)
        Text("Force A → mobilité → force B → cardio → force A → mobilité → cardio. Le parcours avance avec les séances terminées, sans rattrapage des jours manqués.")
        Text("Après une séance de force, la suivante reste douce si tu reviens dès le lendemain. Une journée douce choisie librement conserve ta place dans le parcours.")
        Text("Ta progression", style = MaterialTheme.typography.titleLarge)
        Text("Après 3 séances de force faciles au même palier, le temps d’effort augmente de 5 secondes. Une séance difficile fait redescendre d’un palier. Les repos restent de 15 secondes ; aucune charge n’augmente automatiquement.")
        Text("Au palier 3, progresse d’abord en contrôle et en répétitions. Quand cela reste facile, augmente légèrement l’haltère sur un seul mouvement, en gardant 2 à 3 répétitions en réserve. Maximum disponible : 13 kg, pas un objectif obligatoire.")
        Text("Au programme", style = MaterialTheme.typography.titleLarge)
        plan.steps.filter { it.kind == StepKind.WORK }.distinctBy { it.name }.forEach { step ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(step.name, fontWeight = FontWeight.Bold)
                    Text(step.cue)
                    Text("Plus doux : ${step.easier}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Text("Pour rester en forme longtemps", style = MaterialTheme.typography.titleLarge)
        Text("Ces séances complètent la marche et les déplacements actifs. Au fil de la semaine, vise aussi du mouvement qui accélère un peu la respiration. Bouger régulièrement compte davantage qu’une séance parfaite.")
        Text("Si une douleur apparaît, arrête le mouvement. Tu peux faire une pause à tout moment.", style = MaterialTheme.typography.bodySmall)
    }
}
