package com.example.futboldata.data.repository.impl

import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.repository.JugadorRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class JugadorRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : JugadorRepository {

    override suspend fun addJugador(jugador: Jugador) {
        db.collection("jugadores")
            .add(jugador.toFirestoreMap())
            .await()
    }

    override suspend fun getJugadoresPorEquipo(equipoId: String): List<Jugador> {
        return db.collection("jugadores")
            .whereEqualTo("equipoId", equipoId)
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                val golesMap = when (val goles = document.get("golesPorCompeticion")) {
                    is Map<*, *> -> goles.mapNotNull { (key, value) ->
                        if (key is String && value is Int) key to value else null
                    }.toMap()
                    else -> null
                }

                Jugador.fromFirestore(
                    id = document.id,
                    nombre = document.getString("nombre") ?: "",
                    posicion = document.getString("posicion") ?: "PO",
                    equipoId = document.getString("equipoId") ?: "",
                    goles = golesMap
                )
            }
    }
}