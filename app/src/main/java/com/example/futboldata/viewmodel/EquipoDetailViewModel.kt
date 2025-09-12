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

    private val _competicionSeleccionada = MutableLiveData<Competicion?>()
    val competicionSeleccionada: LiveData<Competicion?> get() = _competicionSeleccionada

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun cargarEquipo(equipoId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d("DEBUG_VM", "▶ [ViewModel] Cargando partidos para equipo: $equipoId")
                val partidos = partidoRepository.getPartidos(equipoId)
                Log.d("DEBUG_VM", "✓ [ViewModel] Partidos obtenidos: ${partidos.size}")

                repository.getEquipoWithStats(equipoId, partidos).let { (equipo, stats) ->
                    _equipo.value = equipo?.also {
                        Log.d("DEBUG_VM", "✓ [ViewModel] Equipo cargado: ${it.nombre}")
                    }
                    _estadisticas.value = stats?.also {
                        Log.d("DEBUG_VM", "✓ [ViewModel] Stats calculadas: ${it.partidosJugados} partidos")
                    }
                    _partidos.value = partidos
                }

                cargarJugadores(equipoId)
                cargarCompeticiones()
            } catch (e: Exception) {
                Log.e("DEBUG_VM", "✕ [ViewModel] Error en cargarEquipo: ${e.message}")
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

    fun desactivarJugador(jugador: Jugador) {
        viewModelScope.launch {
            try {
                jugadorRepository.eliminarJugador(jugador.id)
                cargarJugadores(jugador.equipoId)
                Log.d("DEBUG_VIEWMODEL", "Jugador desactivado: ${jugador.nombre}")
            } catch (e: Exception) {
                Log.e("DEBUG_VIEWMODEL", "Error al desactivar jugador", e)
                _errorMessage.value = "Error al desactivar jugador: ${e.message}"
            }
        }
    }

    fun getCompetitionName(compId: String): String {
        return competiciones.value?.find { it.id == compId }?.nombre ?: "Competición $compId"
    }

    fun seleccionarCompeticion(competicion: Competicion?) {
        _competicionSeleccionada.value = competicion
    }
}