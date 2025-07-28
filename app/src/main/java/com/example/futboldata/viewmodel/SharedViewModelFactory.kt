package com.example.futboldata.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.futboldata.data.repository.AuthRepository
import com.example.futboldata.data.repository.EquipoRepository
import com.example.futboldata.data.repository.JugadorRepository
import com.example.futboldata.data.repository.PartidoRepository

class SharedViewModelFactory(
    private val equipoRepository: EquipoRepository,
    private val authRepository: AuthRepository,
    private val partidoRepository: PartidoRepository,
    private val jugadorRepository: JugadorRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(EquipoViewModel::class.java) -> {
                EquipoViewModel(equipoRepository) as T
            }
            modelClass.isAssignableFrom(EquipoDetailViewModel::class.java) -> {
                EquipoDetailViewModel(equipoRepository, jugadorRepository, partidoRepository) as T
            }
            modelClass.isAssignableFrom(StatsViewModel::class.java) -> {
                StatsViewModel(equipoRepository) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(PartidoViewModel::class.java) -> {
                PartidoViewModel(partidoRepository, jugadorRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}