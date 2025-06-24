package com.example.futboldata.data.model

data class Competicion(
    val id: String = "",
    val nombre: String,
    val temporada: String,  // Ej: "2023-2024"
    val tipo: TipoCompeticion, // Enum: LIGA, COPA, INTERNACIONAL, AMISTOSO
    val logoUrl: String = ""
)