package com.example.futboldata.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futboldata.data.model.Equipo
import com.example.futboldata.data.model.Estadisticas
import com.example.futboldata.data.repository.EquipoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EquipoDetailViewModel(
    private val equipoRepository: EquipoRepository
) : ViewModel() {

    private val _equipoWithStats = MutableStateFlow<Pair<Equipo, Estadisticas>?>(null)
    val equipoWithStats = _equipoWithStats.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun loadEquipoData(equipoId: String) {
        viewModelScope.launch {
            try {
                val data = equipoRepository.getEquipoWithStats(equipoId)
                _equipoWithStats.value = data
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar equipo: ${e.message}"
            }
        }
    }
}