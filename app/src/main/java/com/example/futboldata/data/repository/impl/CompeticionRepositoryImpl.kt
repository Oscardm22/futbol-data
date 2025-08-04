package com.example.futboldata.data.repository.impl

import android.util.Log
import com.example.futboldata.data.model.Competicion
import com.example.futboldata.data.repository.CompeticionRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CompeticionRepositoryImpl(
    private val db: FirebaseFirestore
) : CompeticionRepository {

    override suspend fun getCompeticiones(): List<Competicion> {
        return try {
            db.collection("competiciones")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(Competicion::class.java)?.copy(id = doc.id)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun saveCompeticion(competicion: Competicion): String {
        return try {
            if (competicion.id.isEmpty()) {
                // Nueva competición
                db.collection("competiciones")
                    .add(competicion)
                    .await()
                    .id
            } else {
                // Actualización
                db.collection("competiciones")
                    .document(competicion.id)
                    .set(competicion)
                    .await()
                competicion.id
            }
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun updateCompeticion(competicion: Competicion) {
        try {
            db.collection("competiciones")
                .document(competicion.id)
                .set(competicion)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun deleteCompeticion(id: String) {
        try {
            db.collection("competiciones")
                .document(id)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("CompeticionRepo", "Error al eliminar", e)
        }
    }
}