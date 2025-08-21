package com.example.futboldata.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    // Almacena los goles y asistencias como mapas
    private val golesMap = mutableMapOf<String, Int>()
    private val asistenciasMap = mutableMapOf<String, Int>()

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

    fun agregarGol(jugadorId: String) {
        val golesActuales = golesMap[jugadorId] ?: 0
        golesMap[jugadorId] = golesActuales + 1
    }

    fun agregarAsistencia(jugadorId: String) {
        val asistenciasActuales = asistenciasMap[jugadorId] ?: 0
        asistenciasMap[jugadorId] = asistenciasActuales + 1
    }

    fun obtenerGoles(): Map<String, Int> = golesMap.toMap()

    fun obtenerAsistencias(): Map<String, Int> = asistenciasMap.toMap()

    fun guardarPartido(partido: Partido) {
        viewModelScope.launch {
            val goleadoresIds = golesMap.flatMap { (jugadorId, cantidad) ->
                List(cantidad) { jugadorId }
            }

            val asistentesIds = asistenciasMap.flatMap { (jugadorId, cantidad) ->
                List(cantidad) { jugadorId }
            }

            // Actualizamos el partido con la alineación actual
            val partidoActualizado = partido.copy(
                alineacionIds = obtenerAlineacionActual(),
                goleadoresIds = goleadoresIds,
                asistentesIds = asistentesIds
            )
            partidoRepository.addPartido(partidoActualizado)
        }
    }

    // Métodos para limpiar o resetear datos si es necesario
    fun limpiarDatos() {
        jugadoresTitulares.clear()
        golesMap.clear()
        asistenciasMap.clear()
    }

    // Métodos para obtener cantidades específicas
    fun obtenerCantidadGoles(jugadorId: String): Int {
        return golesMap[jugadorId] ?: 0
    }

    fun obtenerCantidadAsistencias(jugadorId: String): Int {
        return asistenciasMap[jugadorId] ?: 0
    }
}