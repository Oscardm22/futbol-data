package com.example.futboldata.data.managers

import com.example.futboldata.data.model.Posicion

object PosicionManager {
    private val _posiciones = Posicion.getAll()
    val posiciones: List<Posicion> get() = _posiciones

    fun getPosicionesFiltradas(paraPortero: Boolean = false): List<Posicion> {
        return if (paraPortero) {
            _posiciones.filter { it == Posicion.PO }
        } else {
            _posiciones.filter { it != Posicion.PO }
        }
    }
}