package com.example.futboldata.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futboldata.data.model.Estadisticas
import com.example.futboldata.data.repository.EquipoRepository
import kotlinx.coroutines.launch

class StatsViewModel(
    private val equipoRepository: EquipoRepository
) : ViewModel() {
    private val _statsState = MutableLiveData<StatsState>()
    val statsState: LiveData<StatsState> = _statsState

    sealed class StatsState {
        object Loading : StatsState()
        data class Success(val data: Estadisticas) : StatsState()
        data class Error(val message: String) : StatsState()
    }

    fun loadStats(equipoId: String) {
        _statsState.value = StatsState.Loading
        viewModelScope.launch {
            try {
                val (_, stats) = equipoRepository.getEquipoWithStats(equipoId)
                _statsState.value = StatsState.Success(stats)
            } catch (e: Exception) {
                _statsState.value = StatsState.Error(e.message ?: "Error al cargar estadísticas")
            }
        }
    }
}