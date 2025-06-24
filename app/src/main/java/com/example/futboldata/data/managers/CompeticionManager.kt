package com.example.futboldata.data.managers

import com.example.futboldata.data.model.Competicion
import com.example.futboldata.data.repository.CompeticionRepository
import java.util.Calendar

class CompeticionManager(
    private val repository: CompeticionRepository
) {
    suspend fun getCompeticionesActivas(): List<Competicion> {
        return repository.getCompeticiones()
            .filter { it.temporada == getTemporadaActual() }
    }

    private fun getTemporadaActual(): String {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        return "$year-${year + 1}"
    }
}