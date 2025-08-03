package com.example.futboldata.data.model

enum class TipoCompeticion {
    LIGA,
    COPA_NACIONAL,
    COPA_INTERNACIONAL,
    SUPERCOPA
}

fun TipoCompeticion.toDisplayName(): String {
    return name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
}