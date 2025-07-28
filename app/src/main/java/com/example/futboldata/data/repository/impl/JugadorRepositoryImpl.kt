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
            .add(jugador.toMap())
            .await()
    }

    override suspend fun getJugadoresPorEquipo(equipoId: String): List<Jugador> {
        return db.collection("jugadores")
            .whereEqualTo("equipoId", equipoId)
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(Jugador::class.java)
            }
    }
}