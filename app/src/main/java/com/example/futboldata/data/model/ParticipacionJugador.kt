package com.example.futboldata.data.model

data class ParticipacionJugador(
    val jugadorId: String,
    val goles: Int = 0,
    val asistencias: Int = 0,
    val minutosJugados: Int = 0,
    val esTitular: Boolean,
    val tarjetasAmarillas: Int = 0,
    val tarjetasRojas: Int = 0
)