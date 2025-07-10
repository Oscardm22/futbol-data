package com.example.futboldata.data.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun login(email: String, password: String): Result<FirebaseUser>
    fun sendPasswordResetEmail(email: String)
    fun logout()
}