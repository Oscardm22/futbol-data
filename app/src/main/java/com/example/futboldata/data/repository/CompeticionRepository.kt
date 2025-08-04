package com.example.futboldata.data.repository

import com.example.futboldata.data.model.Competicion

interface CompeticionRepository {
    // Obtener todas las competiciones
    suspend fun getCompeticiones(): List<Competicion>

    // Añadir nueva competición
    suspend fun saveCompeticion(competicion: Competicion): String

    // Actualizar competición
    suspend fun updateCompeticion(competicion: Competicion)

    // Eliminar competición
    suspend fun deleteCompeticion(id: String)
}