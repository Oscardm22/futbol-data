package com.example.futboldata.data.repository

import com.example.futboldata.data.model.Competicion

interface CompeticionRepository {
    suspend fun getCompeticiones(): List<Competicion>
    suspend fun getCompeticionById(id: String): Competicion?
    suspend fun addCompeticion(competicion: Competicion)
}