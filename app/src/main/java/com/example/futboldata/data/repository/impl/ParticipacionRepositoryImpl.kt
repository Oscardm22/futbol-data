package com.example.futboldata.data.repository.impl

import com.example.futboldata.data.model.ParticipacionJugador
import com.example.futboldata.data.repository.ParticipacionRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class ParticipacionRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ParticipacionRepository {

    override suspend fun getParticipacionesPorPartido(partidoId: String): List<ParticipacionJugador> {
        return try {
            db.collection("participaciones")
                .whereEqualTo("partidoId", partidoId)
                .get()
                .await()
                .toObjects(ParticipacionJugador::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getParticipacionesPorJugador(jugadorId: String): List<ParticipacionJugador> {
        return try {
            db.collection("participaciones")
                .whereEqualTo("jugadorId", jugadorId)
                .orderBy("minutosJugados", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(ParticipacionJugador::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addParticipacion(participacion: ParticipacionJugador): String {
        return try {
            val participacionData = hashMapOf(
                "partidoId" to participacion.partidoId,
                "jugadorId" to participacion.jugadorId,
                "minutosJugados" to participacion.minutosJugados,
                "esTitular" to participacion.esTitular,
                "goles" to participacion.goles,
                "asistencias" to participacion.asistencias,
                "tarjetasAmarillas" to participacion.tarjetasAmarillas,
                "tarjetasRojas" to participacion.tarjetasRojas
            )

            db.collection("participaciones")
                .add(participacionData)
                .await()
                .id
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun updateParticipacion(participacion: ParticipacionJugador) {
        try {
            val participacionData = hashMapOf(
                "minutosJugados" to participacion.minutosJugados,
                "esTitular" to participacion.esTitular,
                "goles" to participacion.goles,
                "asistencias" to participacion.asistencias,
                "tarjetasAmarillas" to participacion.tarjetasAmarillas,
                "tarjetasRojas" to participacion.tarjetasRojas
            )

            db.collection("participaciones")
                .document(participacion.id)
                .update(participacionData as Map<String, Any>)
                .await()
        } catch (e: Exception) {
            // Log del error
        }
    }

    override suspend fun deleteParticipacion(participacionId: String) {
        try {
            db.collection("participaciones")
                .document(participacionId)
                .delete()
                .await()
        } catch (e: Exception) {
            // Log del error
        }
    }

    override suspend fun getParticipacion(participacionId: String): ParticipacionJugador? {
        return try {
            db.collection("participaciones")
                .document(participacionId)
                .get()
                .await()
                .toObject(ParticipacionJugador::class.java)
                ?.copy(id = participacionId)
        } catch (e: Exception) {
            null
        }
    }
}