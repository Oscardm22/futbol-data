package com.example.futboldata.data.model

data class Gol(
    val id: String = "",
    val partidoId: String = "",
    val jugadorId: String = "",
    val jugadorNombre: String = "",
    val minuto: Int = 0,
    val tipo: String = "Normal"  // "Penalti", "Cabeza", etc.
) {
    constructor() : this("", "", "", "", 0, "Normal")
}