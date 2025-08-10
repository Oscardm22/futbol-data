package com.example.futboldata.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futboldata.data.model.Competicion
import com.example.futboldata.data.model.Equipo
import com.example.futboldata.data.model.Estadisticas
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.model.Partido
import com.example.futboldata.data.repository.CompeticionRepository
import com.example.futboldata.data.repository.EquipoRepository
import com.example.futboldata.data.repository.JugadorRepository
import com.example.futboldata.data.repository.PartidoRepository
import kotlinx.coroutines.launch

class EquipoDetailViewModel(
    private val repository: EquipoRepository,
    private val jugadorRepository: JugadorRepository,
    private val partidoRepository: PartidoRepository,
    private val competicionRepository: CompeticionRepository
) : ViewModel() {

    private val _equipo = MutableLiveData<Equipo>()
    val equipo: LiveData<Equipo> = _equipo

    private val _estadisticas = MutableLiveData<Estadisticas>()
    val estadisticas: LiveData<Estadisticas> = _estadisticas

    private val _jugadores = MutableLiveData<List<Jugador>>()
    val jugadores: LiveData<List<Jugador>> = _jugadores

    private val _partidos = MutableLiveData<List<Partido>>()
    val partidos: LiveData<List<Partido>> = _partidos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _competiciones = MutableLiveData<List<Competicion>>()
    val competiciones: LiveData<List<Competicion>> = _competiciones

    private val _jugadorAdded = MutableLiveData<String>()

    fun cargarEquipo(equipoId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 1. Obtenemos los partidos primero
                val partidos = partidoRepository.getPartidos(equipoId)

                // 2. Pasamos los partidos al método getEquipoWithStats
                repository.getEquipoWithStats(equipoId, partidos).let { (equipo, stats) ->
                    _equipo.value = equipo ?: Equipo()
                    _estadisticas.value = stats ?: Estadisticas.empty()
                    _partidos.value = partidos // Actualizamos los partidos en el LiveData
                }

                cargarJugadores(equipoId)
                cargarCompeticiones()
            } catch (e: Exception) {
                _equipo.value = Equipo()
                _estadisticas.value = Estadisticas.empty()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarJugadores(equipoId: String) {
        viewModelScope.launch {
            try {
                Log.d("DEBUG_VIEWMODEL", "Cargando jugadores para equipo: $equipoId")
                val lista = jugadorRepository.getJugadoresPorEquipo(equipoId)
                Log.d("DEBUG_VIEWMODEL", "Jugadores obtenidos: ${lista.size}")
                _jugadores.value = lista
            } catch (e: Exception) {
                Log.e("DEBUG_VIEWMODEL", "Error cargando jugadores", e)
                _jugadores.value = emptyList()
            }
        }
    }

    fun addJugador(jugador: Jugador) {
        viewModelScope.launch {
            try {
                val jugadorId = jugadorRepository.addJugador(jugador)
                _jugadorAdded.value = jugadorId
                cargarJugadores(jugador.equipoId)
            } catch (e: Exception) {
                Log.e("EquipoDetailVM", "Error al añadir jugador", e)
            }
        }
    }

    fun addPartido(partido: Partido) {
        viewModelScope.launch {
            try {
                partidoRepository.addPartido(partido)
                // Actualizar la lista de partidos después de añadir uno nuevo
                cargarEquipo(partido.equipoId)
            } catch (e: Exception) {
                Log.e("EquipoDetailVM", "Error al añadir partido", e)
                // Puedes manejar el error aquí (mostrar mensaje, etc.)
            }
        }
    }

    fun cargarCompeticiones() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _competiciones.value = competicionRepository.getCompeticiones()
            } catch (e: Exception) {
                _competiciones.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}