package com.example.futboldata.data.repository

import com.example.futboldata.data.model.Competicion
import com.example.futboldata.data.model.Equipo
import com.example.futboldata.data.model.Estadisticas
import com.example.futboldata.data.model.Partido

interface EquipoRepository {
    suspend fun getEquipos(): List<Equipo>
    suspend fun getEquipoById(equipoId: String): Equipo?
    suspend fun saveEquipo(equipo: Equipo): String
    suspend fun deleteEquipo(equipoId: String)
    suspend fun getCompeticiones(): List<Competicion>
    suspend fun getEquipoWithStats(equipoId: String, partidos: List<Partido>): Pair<Equipo?, Estadisticas?>
}