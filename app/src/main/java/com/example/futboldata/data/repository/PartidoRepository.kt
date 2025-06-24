package com.example.futboldata.data.repository

import com.example.futboldata.data.model.Partido

interface PartidoRepository {
    suspend fun addPartido(partido: Partido)
}