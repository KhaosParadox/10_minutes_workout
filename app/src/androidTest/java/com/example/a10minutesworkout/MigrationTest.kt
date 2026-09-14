package com.example.a10minutesworkout

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.a10minutesworkout.data.WorkoutDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test

class MigrationTest {
    @Test fun versionFourKeepsHistoryAndPlaylist() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "migration-test-${System.nanoTime()}"
        context.openOrCreateDatabase(name, 0, null).use { old ->
            old.execSQL("CREATE TABLE workout_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp INTEGER NOT NULL, dateString TEXT NOT NULL, workoutName TEXT NOT NULL, durationInSeconds INTEGER NOT NULL)")
            old.execSQL("CREATE TABLE tracks (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, uriString TEXT NOT NULL, orderIndex INTEGER NOT NULL)")
            old.execSQL("INSERT INTO workout_sessions VALUES (1, 100, '2026-09-01', 'Ancienne séance', 480)")
            old.execSQL("INSERT INTO tracks VALUES (1, 'Ma musique', 'content://test/music', 0)")
            old.version = 4
        }
        val db = Room.databaseBuilder(context, WorkoutDatabase::class.java, name).addMigrations(WorkoutDatabase.MIGRATION_4_5).build()
        try {
            val history = db.workoutDao().getAllSessions().first()
            assertEquals(1, history.size); assertEquals(480, history.first().durationInSeconds)
            assertEquals("", history.first().programId)
            assertEquals("Ma musique", db.trackDao().getAllTracks().first().single().title)
            val session = history.first().copy(id = 0, sessionKey = "unique", programId = "strength_a")
            db.workoutDao().insertSession(session); db.workoutDao().insertSession(session)
            assertEquals(2, db.workoutDao().getAllSessions().first().size)
        } finally { db.close(); context.deleteDatabase(name) }
    }
}
