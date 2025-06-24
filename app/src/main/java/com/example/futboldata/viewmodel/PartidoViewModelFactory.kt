package com.example.futboldata.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.futboldata.data.repository.JugadorRepository
import com.example.futboldata.data.repository.PartidoRepository

class PartidoViewModelFactory(
    private val partidoRepository: PartidoRepository,
    private val jugadorRepository: JugadorRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PartidoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PartidoViewModel(partidoRepository, jugadorRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}