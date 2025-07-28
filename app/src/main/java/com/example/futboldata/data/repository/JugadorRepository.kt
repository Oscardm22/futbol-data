package com.example.futboldata.data.repository

import com.example.futboldata.data.model.Jugador

interface JugadorRepository {
    suspend fun addJugador(jugador: Jugador)
    suspend fun getJugadoresPorEquipo(equipoId: String): List<Jugador>
}