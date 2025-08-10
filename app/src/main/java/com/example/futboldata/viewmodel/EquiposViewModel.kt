package com.example.futboldata.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futboldata.data.model.Equipo
import com.example.futboldata.data.model.Estadisticas
import com.example.futboldata.data.model.Partido
import com.example.futboldata.data.repository.EquipoRepository
import com.example.futboldata.data.repository.PartidoRepository
import kotlinx.coroutines.launch

class EquipoViewModel(
    private val repository: EquipoRepository,
    private val partidoRepository: PartidoRepository
) : ViewModel() {

    // Estados para la lista de equipos
    private val _equiposState = MutableLiveData<EquipoState>()
    val equiposState: LiveData<EquipoState> = _equiposState

    // Estados para operaciones CRUD
    private val _operacionState = MutableLiveData<OperacionState>()
    val operacionState: LiveData<OperacionState> = _operacionState

    // Estado para equipos con sus estadísticas calculadas
    private val _equiposConStats = MutableLiveData<List<Pair<Equipo, Estadisticas>>>()
    val equiposConStats: LiveData<List<Pair<Equipo, Estadisticas>>> = _equiposConStats


    init {
        cargarEquipos()
    }

    fun cargarEquipos() {
        viewModelScope.launch {
            _equiposState.value = EquipoState.Loading
            try {
                val equipos = repository.getEquipos()

                // Calcular estadísticas para cada equipo
                val equiposYStats = equipos.map { equipo ->
                    val partidos = partidoRepository.getPartidos(equipo.id)
                    val stats = calcularEstadisticas(partidos)
                    Pair(equipo, stats)
                }

                _equiposConStats.value = equiposYStats
                _equiposState.value = EquipoState.Success(equipos)
            } catch (e: Exception) {
                _equiposState.value = EquipoState.Error(e.localizedMessage ?: "Error al cargar equipos")
            }
        }
    }

    private fun calcularEstadisticas(partidos: List<Partido>): Estadisticas {
        val partidosJugados = partidos.size
        val victorias = partidos.count { it.fueVictoria() }
        val empates = partidos.count { it.obtenerEstadoPartido() == "Empate" }
        val derrotas = partidos.count { it.obtenerEstadoPartido() == "Derrota" }
        val golesFavor = partidos.sumOf { it.golesEquipo }
        val golesContra = partidos.sumOf { it.golesRival }

        return Estadisticas(
            partidosJugados = partidosJugados,
            victorias = victorias,
            empates = empates,
            derrotas = derrotas,
            golesFavor = golesFavor,
            golesContra = golesContra,
            promedioGoles = if (partidosJugados > 0) golesFavor.toDouble() / partidosJugados else 0.0,
            porcentajeVictorias = if (partidosJugados > 0) (victorias.toDouble() / partidosJugados) * 100 else 0.0
        )
    }

    fun guardarEquipo(equipo: Equipo) {
        viewModelScope.launch {
            _operacionState.value = OperacionState.Loading
            try {
                val id = repository.saveEquipo(equipo)
                if (id.isNotEmpty()) {
                    _operacionState.value = OperacionState.Success(
                        if (equipo.id.isEmpty()) "Equipo creado con ID: $id"
                        else "Equipo actualizado"
                    )
                    cargarEquipos()
                } else {
                    _operacionState.value = OperacionState.Error("Error al obtener ID")
                }
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
        data class Success(val equipo: Equipo?, val stats: Estadisticas?) : EquipoStatsState()
        data class Error(val mensaje: String) : EquipoStatsState()
    }
}