package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository to handle game saving and loading to/from Firebase Firestore.
 * Executed on Dispatchers.IO to prevent freezing or lagging the UI.
 * Integrates error handling with standard Kotlin [Result] pattern.
 */
class SaveGameRepository {
    private val firestore: FirebaseFirestore? get() = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }

    suspend fun saveGameToCloud(userId: String, gameState: PlayerGameState): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firebase Firestore tidak diinisialisasi."))
        try {
            db.collection("saves")
                .document(userId)
                .set(gameState)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadGameFromCloud(userId: String): Result<PlayerGameState> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firebase Firestore tidak diinisialisasi."))
        try {
            val document = db.collection("saves")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val gameState = document.toObject(PlayerGameState::class.java)
                if (gameState != null) {
                    Result.success(gameState)
                } else {
                    Result.failure(Exception("Format penyimpanan tidak valid."))
                }
            } else {
                Result.failure(Exception("Belum ada data backup cloud untuk akun ini."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
