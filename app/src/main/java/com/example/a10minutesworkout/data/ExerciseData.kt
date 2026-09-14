package com.example.a10minutesworkout.data

import com.example.a10minutesworkout.model.Exercise

object ExerciseData {
    val workoutExercises = listOf(
        Exercise(1, "Jumping Jacks", gifResourceName = "jumping_jacks"),
        Exercise(2, "Chaise murale (Wall Sit)", gifResourceName = "wall_sit"),
        Exercise(3, "Pompes (Push-ups)", gifResourceName = "push_ups"),
        Exercise(4, "Bicycle Crunches", gifResourceName = "bicycle_crunches"),
        Exercise(5, "Step-up sur chaise", gifResourceName = "step_up"),
        Exercise(6, "Superman avec tirage", gifResourceName = "superman"),
        Exercise(7, "Squats", gifResourceName = "squats"),
        Exercise(8, "Dips sur chaise", gifResourceName = "dips"),
        Exercise(9, "Pont fessier (Glute Bridge)", gifResourceName = "glute_bridge"),
        Exercise(10, "Planche (Plank)", gifResourceName = "plank"),
        Exercise(11, "Montées de genoux (High Knees)", gifResourceName = "high_knees"),
        Exercise(12, "Fentes alternées (Lunges)", gifResourceName = "lunges"),
        Exercise(13, "Pompes avec rotation (Push-up & Rotation)", gifResourceName = "push_up_rotation"),
        Exercise(14, "Planche latérale gauche (Side Plank Left)", gifResourceName = "side_plank_left"),
        Exercise(15, "Planche latérale droite (Side Plank Right)", gifResourceName = "side_plank_right"),
        Exercise(16, "Burpees", gifResourceName = "burpees")
    )
}
