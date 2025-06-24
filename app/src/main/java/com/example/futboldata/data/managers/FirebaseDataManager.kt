package com.example.futboldata.data.managers

import com.example.futboldata.data.model.Competicion
import com.example.futboldata.data.model.Equipo
import com.example.futboldata.data.model.Partido
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java

class FirebaseDataManager {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getEquipos(): List<Equipo> {
        return db.collection("equipos")
            .get()
            .await()
            .toObjects(Equipo::class.java)
    }

    suspend fun getEquipoById(equipoId: String): Equipo {
        return db.collection("equipos")
            .document(equipoId)
            .get()
            .await()
            .toObject(Equipo::class.java) ?: throw Exception("Equipo no encontrado")
    }

    suspend fun saveEquipo(equipo: Equipo): String {
        val docRef = db.collection("equipos")
            .add(equipo)
            .await()
        return docRef.id
    }

    suspend fun deleteEquipo(equipoId: String) {
        db.collection("equipos")
            .document(equipoId)
            .delete()
            .await()
    }

    suspend fun getPartidos(equipoId: String): List<Partido> {
        return db.collection("partidos")
            .whereEqualTo("equipoId", equipoId)
            .get()
            .await()
            .toObjects(Partido::class.java)
    }

    suspend fun getUltimosPartidos(equipoId: String, limit: Int): List<Partido> {
        return db.collection("partidos")
            .whereEqualTo("equipoId", equipoId)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .toObjects(Partido::class.java)
    }

    // data/managers/FirebaseDataManager.kt
    suspend fun addPartido(partido: Partido): String {
        val partidoMap = hashMapOf(
            "equipoId" to partido.equipoId,
            "fecha" to com.google.firebase.Timestamp(partido.fecha), // Conversión especial para Firestore
            "rival" to partido.rival,
            "resultado" to partido.resultado,
            "competicionId" to partido.competicionId,
            "competicionNombre" to partido.competicionNombre,
            "temporada" to partido.temporada,
            "fase" to partido.fase,
            "jornada" to partido.jornada,
            // No incluimos goleadores y asistentes directamente (se manejarán en subcolecciones)
            "jugadorDelPartido" to partido.jugadorDelPartido
        )

        val docRef = db.collection("partidos")
            .add(partidoMap)
            .await()

        return docRef.id
    }

    suspend fun getCompeticionesFromFirebase(): List<Competicion> {
        return db.collection("competiciones")
            .get()
            .await()
            .toObjects(Competicion::class.java)
    }

}