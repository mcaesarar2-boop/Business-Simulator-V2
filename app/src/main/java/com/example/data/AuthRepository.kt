package com.example.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Repository to handle user authentication using Firebase Authentication.
 * Uses Kotlin Coroutines and the play-services-tasks library (.await()) to keep things clean and sequential.
 */
class AuthRepository {
    private val firebaseAuth: FirebaseAuth? get() = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null
    }

    suspend fun signUp(email: String, password: String): Result<String> {
        val auth = firebaseAuth ?: return Result.failure(Exception("Firebase Auth tidak diinisialisasi."))
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("Pendaftaran gagal: User ID tidak ditemukan.")
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<String> {
        val auth = firebaseAuth ?: return Result.failure(Exception("Firebase Auth tidak diinisialisasi."))
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("Login gagal: User ID tidak ditemukan.")
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String? {
        return try {
            firebaseAuth?.currentUser?.uid
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUserEmail(): String? {
        return try {
            firebaseAuth?.currentUser?.email
        } catch (e: Exception) {
            null
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            // ignore
        }
    }
}
