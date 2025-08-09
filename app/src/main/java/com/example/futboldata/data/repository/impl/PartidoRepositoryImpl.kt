package com.example.futboldata.data.repository.impl

import com.example.futboldata.data.model.*
import com.example.futboldata.data.repository.PartidoRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*

class PartidoRepositoryImpl(
    private val db: FirebaseFirestore
) : PartidoRepository {

    override suspend fun addPartido(partido: Partido): String {
        val partidoData = hashMapOf(
            "equipoId" to partido.equipoId,
            "fecha" to convertDateToTimestamp(partido.fecha),
            "rival" to partido.rival,
            "golesEquipo" to partido.golesEquipo,
            "golesRival" to partido.golesRival,
            "competicionId" to partido.competicionId,
            "competicionNombre" to partido.competicionNombre,
            "temporada" to partido.temporada,
            "fase" to partido.fase,
            "jornada" to partido.jornada,
            "jugadorDelPartido" to partido.jugadorDelPartido,
            "alineacionIds" to partido.alineacionIds,
            "goleadoresIds" to partido.goleadoresIds,
            "asistentesIds" to partido.asistentesIds
        )

        return db.collection("partidos")
            .add(partidoData)
            .await()
            .id
    }

    override suspend fun getPartidos(equipoId: String): List<Partido> {
        return db.collection("partidos")
            .whereEqualTo("equipoId", equipoId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toPartido() }
    }

    override suspend fun getUltimosPartidos(equipoId: String, limit: Int): List<Partido> {
        return db.collection("partidos")
            .whereEqualTo("equipoId", equipoId)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .documents
            .mapNotNull { it.toPartido() }
    }

    override suspend fun deletePartido(partidoId: String) {
        db.collection("partidos")
            .document(partidoId)
            .delete()
            .await()
    }

    /* Métodos de conversión y parseo */
    private fun convertDateToTimestamp(date: Date): Timestamp {
        return Timestamp(date.time / 1000, ((date.time % 1000) * 1_000_000).toInt())
    }

    private fun DocumentSnapshot.toPartido(): Partido? {
        return try {
            Partido(
                id = id,
                equipoId = getString("equipoId") ?: "",
                fecha = getTimestamp("fecha")?.toDate() ?: Date(),
                rival = getString("rival") ?: "",
                golesEquipo = getLong("golesEquipo")?.toInt() ?: 0,
                golesRival = getLong("golesRival")?.toInt() ?: 0,
                competicionId = getString("competicionId") ?: "",
                competicionNombre = getString("competicionNombre") ?: "",
                temporada = getString("temporada") ?: "",
                fase = getString("fase"),
                jornada = getLong("jornada")?.toInt(),
                esLocal = getBoolean("esLocal") != false,
                jugadorDelPartido = getString("jugadorDelPartido"),
                alineacionIds = parseStringList(get("alineacionIds")),
                goleadoresIds = parseStringList(get("goleadoresIds")),
                asistentesIds = parseStringList(get("asistentesIds"))
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseStringList(data: Any?): List<String> {
        return when (data) {
            is List<*> -> data.filterIsInstance<String>()
            else -> emptyList()
        }
    }
}