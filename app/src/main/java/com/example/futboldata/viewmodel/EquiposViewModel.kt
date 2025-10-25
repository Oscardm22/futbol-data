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
import android.util.Log

class EquipoViewModel(
    private val repository: EquipoRepository,
    private val partidoRepository: PartidoRepository
) : ViewModel() {

    // Estados para la lista de equipos
    private val _equiposState = MutableLiveData<EquipoState>()
    val equiposState: LiveData<EquipoState> = _equiposState

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
                Log.e("EquipoViewModel", "Error al cargar equipos", e)
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
            try {
                val id = repository.saveEquipo(equipo)
                if (id.isNotEmpty()) {
                    cargarEquipos()
                } else {
                    Log.e("EquipoViewModel", "Error: ID vacío al guardar equipo")
                }
            } catch (e: Exception) {
                Log.e("EquipoViewModel", "Error al guardar equipo", e)
            }
        }
    }

    fun eliminarEquipo(equipoId: String) {
        viewModelScope.launch {
            try {
                repository.deleteEquipo(equipoId)
                cargarEquipos() // Refrescar lista
            } catch (e: Exception) {
                Log.e("EquipoViewModel", "Error al eliminar equipo", e)
            }
        }
    }

    // Estados para la UI
    sealed class EquipoState {
        object Loading : EquipoState()
        data class Success(val equipos: List<Equipo>) : EquipoState()
        data class Error(val mensaje: String) : EquipoState()
    }
}