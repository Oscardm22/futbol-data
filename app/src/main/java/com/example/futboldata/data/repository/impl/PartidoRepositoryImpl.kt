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
            "resultado" to partido.resultado,
            "competicionId" to partido.competicionId,
            "competicionNombre" to partido.competicionNombre,
            "temporada" to partido.temporada,
            "fase" to partido.fase,
            "jornada" to partido.jornada,
            "goleadores" to partido.goleadores.map { gol ->
                hashMapOf(
                    "id" to gol.id,
                    "partidoId" to gol.partidoId,
                    "jugadorId" to gol.jugadorId,
                    "jugadorNombre" to gol.jugadorNombre,
                    "minuto" to gol.minuto,
                    "tipo" to gol.tipo
                )
            },
            "asistentes" to partido.asistentes.map { asist ->
                hashMapOf(
                    "id" to asist.id,
                    "partidoId" to asist.partidoId,
                    "jugadorId" to asist.jugadorId,
                    "jugadorNombre" to asist.jugadorNombre,
                    "minuto" to asist.minuto
                )
            },
            "jugadorDelPartido" to partido.jugadorDelPartido,
            "alineacion" to partido.alineacion.map { participacion ->
                hashMapOf(
                    "jugadorId" to participacion.jugadorId,
                    "goles" to participacion.goles,
                    "asistencias" to participacion.asistencias,
                    "minutosJugados" to participacion.minutosJugados,
                    "titular" to participacion.titular,
                    "tarjetasAmarillas" to participacion.tarjetasAmarillas,
                    "tarjetasRojas" to participacion.tarjetasRojas
                )
            }
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
                resultado = getString("resultado") ?: "0-0",
                competicionId = getString("competicionId") ?: "",
                competicionNombre = getString("competicionNombre") ?: "",
                temporada = getString("temporada") ?: "",
                fase = getString("fase") ?: "",
                jornada = getLong("jornada")?.toInt(),
                goleadores = parseGoleadores(get("goleadores")),
                asistentes = parseAsistentes(get("asistentes")),
                jugadorDelPartido = getString("jugadorDelPartido"),
                alineacion = parseAlineacion(get("alineacion")))
        } catch (e: Exception) {
            null
        }
    }

    private fun parseGoleadores(data: Any?): List<Gol> {
        return (data as? List<*>)?.filterIsInstance<Map<String, Any>>()?.mapNotNull {
            try {
                Gol(
                    id = it["id"]?.toString() ?: "",
                    partidoId = it["partidoId"]?.toString() ?: "",
                    jugadorId = it["jugadorId"]?.toString() ?: "",
                    jugadorNombre = it["jugadorNombre"]?.toString() ?: "",
                    minuto = (it["minuto"] as? Number)?.toInt() ?: 0,
                    tipo = it["tipo"]?.toString() ?: "Normal"
                )
            } catch (e: Exception) {
                null
            }
        } ?: emptyList()
    }

    private fun parseAsistentes(data: Any?): List<Asistencia> {
        return (data as? List<*>)?.filterIsInstance<Map<String, Any>>()?.mapNotNull {
            try {
                Asistencia(
                    id = it["id"]?.toString() ?: "",
                    partidoId = it["partidoId"]?.toString() ?: "",
                    jugadorId = it["jugadorId"]?.toString() ?: "",
                    jugadorNombre = it["jugadorNombre"]?.toString() ?: "",
                    minuto = (it["minuto"] as? Number)?.toInt() ?: 0
                )
            } catch (e: Exception) {
                null
            }
        } ?: emptyList()
    }

    private fun parseAlineacion(data: Any?): List<ParticipacionJugador> {
        return (data as? List<*>)?.filterIsInstance<Map<String, Any>>()?.mapNotNull {
            try {
                ParticipacionJugador(
                    jugadorId = it["jugadorId"]?.toString() ?: "",
                    goles = (it["goles"] as? Number)?.toInt() ?: 0,
                    asistencias = (it["asistencias"] as? Number)?.toInt() ?: 0,
                    minutosJugados = (it["minutosJugados"] as? Number)?.toInt() ?: 0,
                    titular = it["titular"] as? Boolean == true,
                    tarjetasAmarillas = (it["tarjetasAmarillas"] as? Number)?.toInt() ?: 0,
                    tarjetasRojas = (it["tarjetasRojas"] as? Number)?.toInt() ?: 0
                )
            } catch (e: Exception) {
                null
            }
        } ?: emptyList()
    }
}