package com.example.futboldata.viewmodel

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

    fun cargarEquipo(equipoId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Cargar equipo y estadísticas
                val (equipoData, statsData) = repository.getEquipoWithStats(equipoId)
                _equipo.value = equipoData
                _estadisticas.value = statsData

                cargarJugadores(equipoId)
                cargarPartidos(equipoId)
                cargarCompeticiones()
            } catch (e: Exception) {
                // Manejo de errores
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarJugadores(equipoId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val listaJugadores = jugadorRepository.getJugadoresPorEquipo(equipoId)
                _jugadores.value = listaJugadores
            } catch (e: Exception) {
                // Manejo de errores
                _jugadores.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addJugador(jugador: Jugador) {
        viewModelScope.launch {
            try {
                jugadorRepository.addJugador(jugador)
                // Actualizar la lista después de añadir
                cargarJugadores(jugador.equipoId)
            } catch (e: Exception) {
                // Manejo de errores
            }
        }
    }

    fun addPartido(partido: Partido) {
        viewModelScope.launch {
            repository.addPartido(partido)
        }
    }

    fun cargarPartidos(equipoId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _partidos.value = partidoRepository.getPartidos(equipoId)
            } catch (e: Exception) {
                _partidos.value = emptyList()
            } finally {
                _isLoading.value = false
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