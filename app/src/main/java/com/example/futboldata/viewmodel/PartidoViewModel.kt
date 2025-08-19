package com.example.futboldata.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futboldata.data.model.Asistencia
import com.example.futboldata.data.model.Gol
import com.example.futboldata.data.model.Jugador
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

    // Almacena los IDs de los jugadores titulares
    private val jugadoresTitulares = mutableSetOf<String>()

    // Almacena los goles y asistencias
    private val goles = mutableListOf<Gol>()
    private val asistencias = mutableListOf<Asistencia>()

    fun cargarJugadores(equipoId: String) {
        viewModelScope.launch {
            _jugadores.value = jugadorRepository.getJugadoresPorEquipo(equipoId)
        }
    }

    fun actualizarTitularidad(jugadorId: String, esTitular: Boolean) {
        if (esTitular) {
            jugadoresTitulares.add(jugadorId)
        } else {
            jugadoresTitulares.remove(jugadorId)
        }
    }

    fun obtenerAlineacionActual(): List<String> {
        return jugadoresTitulares.toList()
    }

    fun agregarGol(gol: Gol) {
        goles.add(gol)
    }

    fun agregarAsistencia(asistencia: Asistencia) {
        asistencias.add(asistencia)
    }

    fun obtenerGoles(): List<Gol> = goles.toList()

    fun obtenerAsistencias(): List<Asistencia> = asistencias.toList()

    fun guardarPartido(partido: Partido) {
        viewModelScope.launch {
            // Actualizamos el partido con la alineación actual
            val partidoActualizado = partido.copy(
                alineacionIds = obtenerAlineacionActual(),
                goleadoresIds = goles.map { it.jugadorId },
                asistentesIds = asistencias.map { it.jugadorId }
            )
            partidoRepository.addPartido(partidoActualizado)
        }
    }
}