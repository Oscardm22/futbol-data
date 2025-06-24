package com.example.futboldata.data.model

data class Asistencia(
    val id: String = "",
    val partidoId: String = "",
    val jugadorId: String = "",
    val jugadorNombre: String = "",
    val minuto: Int = 0
) {
    constructor() : this("", "", "", "", 0)
}