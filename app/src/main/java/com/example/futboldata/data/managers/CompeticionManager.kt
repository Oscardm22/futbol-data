package com.example.futboldata.data.managers

import com.example.futboldata.data.model.Competicion
import com.example.futboldata.data.repository.CompeticionRepository

class CompeticionManager(
    private val repository: CompeticionRepository
) {
    suspend fun getCompeticionesActivas(): List<Competicion> {
        return repository.getCompeticiones()
    }

    suspend fun addCompeticion(competicion: Competicion) {
        repository.saveCompeticion(competicion)
    }
}