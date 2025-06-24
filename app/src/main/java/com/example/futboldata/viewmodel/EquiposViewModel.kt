package com.example.futboldata.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futboldata.data.model.Equipo
import com.example.futboldata.data.model.Estadisticas
import com.example.futboldata.data.repository.EquipoRepository
import kotlinx.coroutines.launch

class EquipoViewModel(
    private val repository: EquipoRepository
) : ViewModel() {

    // Estados para la lista de equipos
    private val _equiposState = MutableLiveData<EquipoState>()
    val equiposState: LiveData<EquipoState> = _equiposState

    // Estados para operaciones CRUD
    private val _operacionState = MutableLiveData<OperacionState>()
    val operacionState: LiveData<OperacionState> = _operacionState

    // Estados para equipo con estadísticas
    private val _equipoStatsState = MutableLiveData<EquipoStatsState>()
    val equipoStatsState: LiveData<EquipoStatsState> = _equipoStatsState

    init {
        cargarEquipos()
    }

    fun cargarEquipos() {
        viewModelScope.launch {
            _equiposState.value = EquipoState.Loading
            try {
                val equipos = repository.getEquipos()
                _equiposState.value = EquipoState.Success(equipos)
            } catch (e: Exception) {
                _equiposState.value = EquipoState.Error(
                    e.localizedMessage ?: "Error al cargar equipos"
                )
            }
        }
    }

    fun guardarEquipo(equipo: Equipo) {
        viewModelScope.launch {
            _operacionState.value = OperacionState.Loading
            try {
                val equipoId = repository.saveEquipo(equipo)
                _operacionState.value = OperacionState.Success("Equipo guardado con ID: $equipoId")
                cargarEquipos() // Refrescar lista
            } catch (e: Exception) {
                _operacionState.value = OperacionState.Error(
                    e.localizedMessage ?: "Error al guardar equipo"
                )
            }
        }
    }

    fun eliminarEquipo(equipoId: String) {
        viewModelScope.launch {
            _operacionState.value = OperacionState.Loading
            try {
                repository.deleteEquipo(equipoId)
                _operacionState.value = OperacionState.Success("Equipo eliminado")
                cargarEquipos() // Refrescar lista
            } catch (e: Exception) {
                _operacionState.value = OperacionState.Error(
                    e.localizedMessage ?: "Error al eliminar equipo"
                )
            }
        }
    }

    fun cargarEquipoConEstadisticas(equipoId: String) {
        viewModelScope.launch {
            _equipoStatsState.value = EquipoStatsState.Loading
            try {
                val (equipo, stats) = repository.getEquipoWithStats(equipoId)
                _equipoStatsState.value = EquipoStatsState.Success(equipo, stats)
            } catch (e: Exception) {
                _equipoStatsState.value = EquipoStatsState.Error(
                    e.localizedMessage ?: "Error al cargar estadísticas"
                )
            }
        }
    }

    // Estados para la UI
    sealed class EquipoState {
        object Loading : EquipoState()
        data class Success(val equipos: List<Equipo>) : EquipoState()
        data class Error(val mensaje: String) : EquipoState()
    }

    sealed class OperacionState {
        object Loading : OperacionState()
        data class Success(val mensaje: String) : OperacionState()
        data class Error(val mensaje: String) : OperacionState()
    }

    sealed class EquipoStatsState {
        object Loading : EquipoStatsState()
        data class Success(val equipo: Equipo, val stats: Estadisticas) : EquipoStatsState()
        data class Error(val mensaje: String) : EquipoStatsState()
    }
}