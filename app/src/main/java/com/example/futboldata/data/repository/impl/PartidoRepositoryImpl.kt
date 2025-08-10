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
        val documentRef = db.collection("partidos").document()
        val partidoConId = partido.copy(id = documentRef.id)

        val partidoData = hashMapOf(
            "id" to documentRef.id,
            "equipoId" to partidoConId.equipoId,
            "fecha" to convertDateToTimestamp(partidoConId.fecha),
            "rival" to partidoConId.rival,
            "golesEquipo" to partidoConId.golesEquipo,
            "golesRival" to partidoConId.golesRival,
            "competicionId" to partidoConId.competicionId,
            "competicionNombre" to partidoConId.competicionNombre,
            "temporada" to partidoConId.temporada,
            "fase" to partidoConId.fase,
            "jornada" to partidoConId.jornada,
            "esLocal" to partidoConId.esLocal,
            "jugadorDelPartido" to partidoConId.jugadorDelPartido,
            "alineacionIds" to partidoConId.alineacionIds,
            "goleadoresIds" to partidoConId.goleadoresIds,
            "asistentesIds" to partidoConId.asistentesIds
        )

        documentRef.set(partidoData).await()
        return documentRef.id
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