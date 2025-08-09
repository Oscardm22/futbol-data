package com.example.futboldata.data.repository

import com.example.futboldata.data.model.ParticipacionJugador

interface ParticipacionRepository {
    suspend fun getParticipacionesPorPartido(partidoId: String): List<ParticipacionJugador>
    suspend fun getParticipacionesPorJugador(jugadorId: String): List<ParticipacionJugador>
    suspend fun addParticipacion(participacion: ParticipacionJugador): String
    suspend fun updateParticipacion(participacion: ParticipacionJugador)
    suspend fun deleteParticipacion(participacionId: String)
    suspend fun getParticipacion(participacionId: String): ParticipacionJugador?
}