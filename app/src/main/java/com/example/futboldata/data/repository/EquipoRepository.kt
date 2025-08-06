package com.example.futboldata.data.repository

import com.example.futboldata.data.model.Competicion
import com.example.futboldata.data.model.Equipo
import com.example.futboldata.data.model.Estadisticas
import com.example.futboldata.data.model.Partido

interface EquipoRepository {
    // Equipos
    suspend fun getEquipos(): List<Equipo>
    suspend fun getEquipoById(equipoId: String): Equipo?
    suspend fun saveEquipo(equipo: Equipo): String
    suspend fun deleteEquipo(equipoId: String)

    // Partidos
    suspend fun getPartidos(equipoId: String): List<Partido>
    suspend fun getUltimosPartidos(equipoId: String, limit: Int): List<Partido>
    suspend fun addPartido(partido: Partido): String

    // Competiciones
    suspend fun getCompeticiones(): List<Competicion>

    // Stats
    suspend fun getEquipoWithStats(equipoId: String): Pair<Equipo?, Estadisticas?>?
}