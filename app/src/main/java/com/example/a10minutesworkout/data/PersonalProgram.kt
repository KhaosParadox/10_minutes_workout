package com.example.a10minutesworkout.data

import com.example.a10minutesworkout.model.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Progress comes from completed sessions, never from missed calendar days. */
object PersonalProgram {
    private val cycle = listOf("strength_a", "mobility", "strength_b", "cardio", "strength_a", "mobility", "cardio")

    fun recommended(history: List<WorkoutSession>, today: LocalDate = LocalDate.now(), gentle: Boolean = false): TrainingPlan {
        val own = history.filter { it.programId.isNotEmpty() }.sortedByDescending { it.timestamp }
        val completed = own.filter { it.completed }
        val recentStrength = completed.filter { it.programId.startsWith("strength") }
        val last = recentStrength.firstOrNull()
        var level = last?.level ?: 0
        if (last?.feedback == "HARD") level--
        else if (recentStrength.take(3).size == 3 && recentStrength.take(3).all { it.level == level && it.feedback == "EASY" }
            && recentStrength.take(3).map { it.dateString }.distinct().size == 3) level++
        if (last != null && ChronoUnit.DAYS.between(LocalDate.parse(last.dateString), today) >= 7) level--
        level = level.coerceIn(0, 2)
        val id = cycle[completed.count { it.advancesCycle } % cycle.size]
        val alreadyToday = completed.any { it.dateString == today.toString() }
        val strengthYesterday = own.any {
            it.programId.startsWith("strength") && it.activeSeconds > 0 &&
                it.dateString in listOf(today.toString(), today.minusDays(1).toString())
        }
        return if (gentle || alreadyToday || (id.startsWith("strength") && strengthYesterday)) {
            build("mobility", level, advances = false)
        } else build(id, level)
    }

    fun build(id: String, level: Int, advances: Boolean = true): TrainingPlan {
        val safeLevel = level.coerceIn(0, 2)
        val strength = id.startsWith("strength")
        val effort = if (strength) 35 + safeLevel * 5 else 40
        val rest = if (strength) 15 else 20
        fun ex(name: String, cue: String, easier: String, gif: String? = null) =
            TrainingStep(name, effort, StepKind.WORK, cue, easier, gif)
        val squat = ex("Squat avec haltère", "Haltère près de la poitrine. Descends en contrôle, pieds bien posés. 6 à 10 répétitions propres, puis souffle.", "Squat sans charge ou assis-debout sur la chaise.")
        fun row(side: String) = ex("Tirage haltère — $side", "Main libre sur la chaise stable, dos neutre. Tire le coude vers la hanche sans tourner le buste. Charge légère au départ.", "Allège l’haltère et réduis l’amplitude.")
        val push = ex("Pompes", "Corps aligné, expire en poussant. Garde 2 à 3 répétitions possibles : le chrono n’oblige pas à bouger sans arrêt.", "Pompes contre un mur.", "push_ups")
        val hinge = ex("Soulevé de terre avec haltère", "Tiens l’haltère à deux mains devant toi. Genoux souples, pousse les hanches en arrière, dos neutre. 6 à 10 répétitions lentes.", "Même mouvement sans charge, petite amplitude.")
        val deadbug = ex("Dead bug alterné", "Sur le dos, genoux au-dessus des hanches. Allonge lentement un bras et la jambe opposée, sans creuser le dos.", "Garde les bras au sol et touche le tapis avec un talon à la fois.")
        val bridge = ex("Pont fessier", "Pieds à plat, pousse dans les talons. Serre les fessiers en haut sans cambrer, puis redescends lentement.", "Monte moins haut, sans charge.", "glute_bridge")
        fun press(side: String) = ex("Développé au sol — $side", "Allongé sur le tapis, pieds au sol. Pousse l’haltère au-dessus de l’épaule, poignet droit. Redescends le coude doucement au sol.", "Charge plus légère. Pose l’haltère au sol avant de changer de côté.")
        fun side(side: String) = ex("Gainage latéral — $side", "Coude sous l’épaule, genoux fléchis au sol. Soulève le bassin en respirant. Jambes tendues si facile.", "Tiens 10 secondes puis repose-toi, plusieurs fois.")
        val march = ex("Marche active", "Marche sur place avec les bras. Respiration plus rapide, mais garde la possibilité de parler.", "Marche tranquillement.")
        val step = ex("Pas latéraux", "Deux pas à gauche, deux à droite. Genoux souples, bras actifs, sans saut.", "Petits pas, rythme lent.")
        val balance = ex("Équilibre alterné", "Près de la chaise, tiens sur un pied puis l’autre. Change de côté à mi-temps, regard fixe.", "Garde un doigt sur la chaise.")
        val movements = when (id) {
            "strength_a" -> listOf(squat, push, row("gauche"), row("droite"), hinge, deadbug)
            "strength_b" -> listOf(
                ex("Fentes arrière alternées", "Recule un pied, descends à une amplitude confortable puis reviens. Alterne, buste stable, sans charge au départ.", "Tiens la chaise et fais une petite flexion."),
                press("gauche"), press("droite"),
                ex("Tirage avec bande", "Bande longue sous les deux pieds, chaussures posées dessus. Hanches en arrière, dos neutre. Tire les mains vers les hanches sans à-coups. Vérifie la bande avant usage.", "Moins de tension ou tirage sans résistance."),
                side("gauche"), side("droite"))
            "cardio" -> listOf(march, step,
                ex("Boxe dans le vide", "Pieds décalés, petits coups de poing contrôlés, sans haltère. Ne verrouille pas les coudes.", "Ralentis, sans déplacement."),
                ex("Squats tranquilles", "Descends sans rebond puis remonte. Rythme régulier, respiration libre.", "Petite amplitude ou chaise.", "squats"), balance)
            else -> listOf(
                ex("Mobilité des épaules", "Debout, fais de petits cercles d’épaules puis lève les bras doucement. Amplitude confortable.", "Petits cercles uniquement."),
                ex("Chat-vache", "À quatre pattes, arrondis puis déroule doucement le dos au rythme de la respiration. Ne force pas les extrêmes.", "Debout, mains sur les cuisses."),
                ex("Rotation du buste assis", "Assis droit sur la chaise, tourne doucement le buste, alternativement à gauche et à droite.", "Réduis la rotation."),
                ex("Mobilité des chevilles", "Debout avec appui, avance doucement un genou au-dessus du pied sans décoller le talon. Alterne les côtés.", "Petite amplitude."), balance)
        }
        val steps = buildList {
            add(TrainingStep("Installe ton espace", 10, StepKind.PREPARE, "Tapis et chaise prêts. Choisis une charge confortable, jamais automatiquement 13 kg."))
            add(TrainingStep("Marche progressive", 60, StepKind.WARMUP, "Commence doucement et augmente progressivement le mouvement des bras."))
            add(TrainingStep("Mobilise tout le corps", 60, StepKind.WARMUP, "Petits squats sans charge, hanches en arrière et cercles d’épaules. Reste à l’aise."))
            repeat(2) { round ->
                movements.forEachIndexed { index, movement ->
                    add(movement)
                    // A transition after every slot includes the side switch and the last recovery.
                    val next = movements.getOrNull(index + 1) ?: if (round == 0) movements.first() else null
                    add(TrainingStep("Souffle et prépare la suite", rest, StepKind.REST,
                        next?.let { "Ensuite : ${it.name}. Pose la charge avant de te déplacer." } ?: "La partie principale est terminée. Ralentis progressivement."))
                }
            }
            add(TrainingStep("Retour au calme", 60, StepKind.COOLDOWN, "Marche lentement puis respire tranquillement. Prolonge si tu en ressens le besoin."))
        }
        return TrainingPlan(id, when (id) {
            "strength_a" -> "Force A · jambes et dos"
            "strength_b" -> "Force B · appuis et gainage"
            "cardio" -> "Cardio et équilibre"
            else -> "Mobilité · journée douce"
        }, safeLevel, if (id == "strength_b") "Haltère · bande longue · chaise · tapis" else if (strength) "Haltère réglable · chaise stable · tapis" else "Chaise stable · tapis",
            if (strength) "Deux passages. Garde 2 à 3 répétitions en réserve. Repose-toi dans le créneau si nécessaire."
            else if (id == "cardio") "Sans saut et sans charge. Tu dois pouvoir parler ; ralentis si nécessaire."
            else "Bouger sans chercher la fatigue. Amplitudes confortables, respiration calme.", steps, advances)
    }
}
