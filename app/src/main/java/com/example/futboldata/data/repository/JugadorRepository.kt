package com.example.futboldata.data.repository

import com.example.futboldata.data.model.Jugador

interface JugadorRepository {
    suspend fun addJugador(jugador: Jugador): String
    suspend fun getJugadoresPorEquipo(equipoId: String): List<Jugador>
    suspend fun eliminarJugador(jugadorId: String)
}