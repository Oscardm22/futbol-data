package com.example.futboldata.data.model

data class Jugador(
    val id: String = "",
    val nombre: String,
    val posicion: Posicion,
    val equipoId: String,
    val goles: Map<TipoCompeticion, Int> = mapOf(
        TipoCompeticion.LIGA to 0,
        TipoCompeticion.COPA_NACIONAL to 0,
        TipoCompeticion.COPA_INTERNACIONAL to 0,
        TipoCompeticion.SUPERCOPA to 0
    )
) {
    val golesTotales: Int
        get() = goles.values.sum()
}
