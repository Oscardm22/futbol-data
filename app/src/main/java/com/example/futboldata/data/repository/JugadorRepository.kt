package com.example.futboldata.data.repository

import com.example.futboldata.data.model.Jugador

interface JugadorRepository {
    suspend fun getJugadoresByEquipo(equipoId: String): List<Jugador>
}