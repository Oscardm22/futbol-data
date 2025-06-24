package com.example.futboldata.data.repository.impl

import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.repository.JugadorRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class JugadorRepositoryImpl(
    private val firestore: FirebaseFirestore
) : JugadorRepository {

    override suspend fun getJugadoresByEquipo(equipoId: String): List<Jugador> {
        return firestore.collection("jugadores")
            .whereEqualTo("equipoId", equipoId)
            .get()
            .await()
            .toObjects(Jugador::class.java)
    }
}