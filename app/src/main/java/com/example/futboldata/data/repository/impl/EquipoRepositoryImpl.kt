package com.example.futboldata.data.repository.impl

import android.util.Log
import com.example.futboldata.data.repository.EquipoRepository
import com.example.futboldata.data.model.*
import com.example.futboldata.utils.StatsCalculator
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java

class EquipoRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val statsCalculator: StatsCalculator
) : EquipoRepository {

    // --- Equipos ---
    override suspend fun getEquipos(): List<Equipo> {
        return try {
            db.collection("equipos")
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    document.toObject(Equipo::class.java)?.copy(id = document.id)
                }
        } catch (e: Exception) {
            Log.e("EquipoRepository", "Error al obtener equipos", e)
            emptyList()
        }
    }

    override suspend fun getEquipoById(equipoId: String): Equipo? {
        return try {
            db.collection("equipos")
                .document(equipoId)
                .get()
                .await()
                .let { document ->
                    if (document.exists()) {
                        document.toObject(Equipo::class.java)?.copy(id = document.id)
                    } else {
                        null
                    }
                }
        } catch (e: Exception) {
            Log.e("EquipoRepository", "Error al obtener equipo", e)
            null
        }
    }

    override suspend fun saveEquipo(equipo: Equipo): String {
        return try {
            if (equipo.id.isEmpty()) {
                // Nuevo equipo
                val docRef = db.collection("equipos")
                    .add(equipo)
                    .await()
                docRef.id
            } else {
                // Equipo existente
                db.collection("equipos")
                    .document(equipo.id)
                    .set(equipo)
                    .await()
                equipo.id
            }
        } catch (e: Exception) {
            Log.e("EquipoRepository", "Error al guardar equipo", e)
            ""
        }
    }

    override suspend fun deleteEquipo(equipoId: String) {
        try {
            db.collection("equipos")
                .document(equipoId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("EquipoRepository", "Error al eliminar equipo", e)
            throw e
        }
    }

    // --- Partidos (sin cambios) ---
    override suspend fun getPartidos(equipoId: String): List<Partido> {
        return db.collection("partidos")
            .whereEqualTo("equipoId", equipoId)
            .get()
            .await()
            .toObjects(Partido::class.java)
    }

    override suspend fun getUltimosPartidos(equipoId: String, limit: Int): List<Partido> {
        return db.collection("partidos")
            .whereEqualTo("equipoId", equipoId)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .toObjects(Partido::class.java)
    }

    override suspend fun addPartido(partido: Partido): String {
        val partidoMap = hashMapOf(
            "equipoId" to partido.equipoId,
            "fecha" to Timestamp(partido.fecha),
            "rival" to partido.rival,
            "resultado" to partido.resultado,
            "competicionId" to partido.competicionId,
            "competicionNombre" to partido.competicionNombre,
            "temporada" to partido.temporada,
            "fase" to partido.fase,
            "jornada" to partido.jornada,
            "jugadorDelPartido" to partido.jugadorDelPartido
        )
        return db.collection("partidos")
            .add(partidoMap)
            .await()
            .id
    }

    // --- Competiciones (sin cambios) ---
    override suspend fun getCompeticiones(): List<Competicion> {
        return db.collection("competiciones")
            .get()
            .await()
            .toObjects(Competicion::class.java)
    }

    // --- Estadísticas ---
    override suspend fun getEquipoWithStats(equipoId: String): Pair<Equipo?, Estadisticas?> {
        return try {
            val equipo = getEquipoById(equipoId)
            val partidos = getPartidos(equipoId)
            val stats = statsCalculator.calculate(partidos)
            Pair(equipo, stats)
        } catch (e: Exception) {
            Log.e("RepoError", "Error al procesar estadísticas:", e)
            Pair(null, null)
        }
    }
}