package com.example.futboldata.data.model

data class ParticipacionJugador(
    val id: String = "",
    val partidoId: String = "",
    val jugadorId: String = "",
    val goles: Int = 0,
    val asistencias: Int = 0,
    val minutosJugados: Int = 0,
    val esTitular: Boolean = false,
    val tarjetasAmarillas: Int = 0,
    val tarjetasRojas: Int = 0
) {
    // Constructor sin parámetros requerido por Firestore
    constructor() : this("", "", "", 0, 0, 0, false, 0, 0)
}