package com.example.futboldata.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.futboldata.data.repository.EquipoRepository

class StatsViewModelFactory(
    private val equipoRepository: EquipoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            return StatsViewModel(equipoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}