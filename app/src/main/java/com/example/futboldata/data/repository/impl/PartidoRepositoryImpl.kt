package com.example.futboldata.data.repository.impl

import com.example.futboldata.data.managers.FirebaseDataManager
import com.example.futboldata.data.model.Partido
import com.example.futboldata.data.repository.PartidoRepository

class PartidoRepositoryImpl(
    private val firebaseManager: FirebaseDataManager
) : PartidoRepository {
    override suspend fun addPartido(partido: Partido) {
        firebaseManager.addPartido(partido)
    }
}