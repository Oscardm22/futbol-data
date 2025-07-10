package com.example.futboldata.data.repository

import com.example.futboldata.data.model.Partido

interface PartidoRepository {
    suspend fun addPartido(partido: Partido): String
    suspend fun getPartidos(equipoId: String): List<Partido>
    suspend fun getUltimosPartidos(equipoId: String, limit: Int): List<Partido>
    suspend fun deletePartido(partidoId: String)
}