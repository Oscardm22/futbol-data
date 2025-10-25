package com.example.futboldata.utils

import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.model.Posicion

object JugadorUtils {
    fun ordenarJugadoresPorPosicion(jugadores: List<Jugador>): List<Jugador> {
        return jugadores.sortedBy { jugador ->
            Posicion.entries.indexOf(jugador.posicion)
        }
    }
}