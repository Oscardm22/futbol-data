package com.example.futboldata.data.repository.impl

import com.example.futboldata.data.repository.EquipoRepository
import com.example.futboldata.data.managers.FirebaseDataManager
import com.example.futboldata.data.managers.StatsCalculator
import com.example.futboldata.data.model.Equipo
import com.example.futboldata.data.model.Estadisticas
import com.example.futboldata.data.model.Partido

class EquipoRepositoryImpl(
    private val firebaseManager: FirebaseDataManager,
    private val statsCalculator: StatsCalculator
) : EquipoRepository {

    // Constructor secundario para inicialización por defecto
    constructor() : this(
        firebaseManager = FirebaseDataManager(),
        statsCalculator = StatsCalculator()
    )

    override suspend fun getEquipos(): List<Equipo> {
        return firebaseManager.getEquipos()
    }

    override suspend fun getEquipoById(equipoId: String): Equipo {
        return firebaseManager.getEquipoById(equipoId)
    }

    override suspend fun saveEquipo(equipo: Equipo): String {
        return firebaseManager.saveEquipo(equipo)
    }

    override suspend fun deleteEquipo(equipoId: String) {
        firebaseManager.deleteEquipo(equipoId)
    }

    override suspend fun getPartidos(equipoId: String): List<Partido> {
        return firebaseManager.getPartidos(equipoId)
    }

    override suspend fun getEquipoWithStats(equipoId: String): Pair<Equipo, Estadisticas> {
        val equipo = getEquipoById(equipoId)
        val partidos = getPartidos(equipoId)
        val stats = statsCalculator.calculate(partidos)
        return Pair(equipo, stats)
    }

    override suspend fun getUltimosPartidos(equipoId: String, limit: Int): List<Partido> {
        return firebaseManager.getUltimosPartidos(equipoId, limit)
    }
}