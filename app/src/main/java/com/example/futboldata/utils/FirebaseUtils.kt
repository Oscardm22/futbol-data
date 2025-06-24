package com.example.futboldata.utils

import android.content.Context
import android.net.Uri
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object FirebaseUtils {
    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context
        FirebaseApp.initializeApp(appContext)
    }

    fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Usuario no autenticado")
    }

    suspend fun uploadTeamBadge(imageUri: Uri): String {
        val storageRef = FirebaseStorage.getInstance().reference
            .child("team_badges/${getCurrentUserId()}/${UUID.randomUUID()}")

        storageRef.putFile(imageUri).await()
        return storageRef.downloadUrl.await().toString()
    }
}