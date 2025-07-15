package com.example.futboldata.data.repository.impl

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
    override suspend fun getEquipos(): Map<String, Equipo> {
        return db.collection("equipos")
            .get()
            .await()
            .documents
            .associate { document ->
                document.id to (document.toObject(Equipo::class.java))!!
            }
    }

    override suspend fun getEquipoById(equipoId: String): Equipo {
        return db.collection("equipos")
            .document(equipoId)
            .get()
            .await()
            .toObject(Equipo::class.java)
            ?: throw Exception("Equipo no encontrado")
    }

    override suspend fun saveEquipo(equipo: Equipo): String {
        val docRef = db.collection("equipos")
            .add(equipo)
            .await()
        return docRef.id
    }

    override suspend fun deleteEquipo(equipoId: String) {
        db.collection("equipos")
            .document(equipoId)
            .delete()
            .await()
    }

    // --- Partidos ---
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

    // --- Competiciones ---
    override suspend fun getCompeticiones(): List<Competicion> {
        return db.collection("competiciones")
            .get()
            .await()
            .toObjects(Competicion::class.java)
    }

    // --- Estadísticas ---
    override suspend fun getEquipoWithStats(equipoId: String): Pair<Equipo, Estadisticas> {
        val equipo = getEquipoById(equipoId)
        val partidos = getPartidos(equipoId)
        val stats = statsCalculator.calculate(partidos)
        return Pair(equipo, stats)
    }
}