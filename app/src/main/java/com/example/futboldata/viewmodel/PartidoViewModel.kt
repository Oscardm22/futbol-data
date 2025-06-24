package com.example.futboldata.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futboldata.data.model.Asistencia
import com.example.futboldata.data.model.Gol
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.model.ParticipacionJugador
import com.example.futboldata.data.model.Partido
import com.example.futboldata.data.repository.JugadorRepository
import com.example.futboldata.data.repository.PartidoRepository
import kotlinx.coroutines.launch

class PartidoViewModel(
    private val partidoRepository: PartidoRepository,
    private val jugadorRepository: JugadorRepository
) : ViewModel() {

    private val _jugadores = MutableLiveData<List<Jugador>>()
    val jugadores: LiveData<List<Jugador>> = _jugadores

    private val participaciones = mutableMapOf<String, ParticipacionJugador>()
    private val goles = mutableListOf<Gol>()
    private val asistencias = mutableListOf<Asistencia>()

    fun cargarJugadores(equipoId: String) {
        viewModelScope.launch {
            _jugadores.value = jugadorRepository.getJugadoresByEquipo(equipoId)
        }
    }

    fun actualizarParticipacion(jugadorId: String, participacion: ParticipacionJugador) {
        participaciones[jugadorId] = participacion
    }

    fun obtenerAlineacionActual(): List<ParticipacionJugador> {
        return _jugadores.value?.map { jugador ->
            participaciones[jugador.id] ?: ParticipacionJugador(
                jugadorId = jugador.id,
                titular = false,
                minutosJugados = 0,
                goles = 0,
                asistencias = 0
            )
        } ?: emptyList()
    }

    fun obtenerGoles(): List<Gol> = goles

    fun obtenerAsistencias(): List<Asistencia> = asistencias

    fun addPartido(partido: Partido) {
        viewModelScope.launch {
            partidoRepository.addPartido(partido)
        }
    }
}