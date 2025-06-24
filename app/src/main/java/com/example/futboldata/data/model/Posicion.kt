package com.example.futboldata.data.model

enum class Posicion(val displayName: String) {
    PO("Portero"),
    DFC("Defensa Central"),
    LD("Lateral Derecho"),
    LI("Lateral Izquierdo"),
    MCD("Mediocampista Defensivo"),
    MC("Mediocampista Central"),
    MCO("Mediocampista Ofensivo"),
    MI("Mediocampista Izquierdo"),
    MD("Mediocampista Derecho"),
    ED("Extremo Derecho"),
    EI("Extremo Izquierdo"),
    DC("Delantero Centro");

    companion object {
        fun getAll(): List<Posicion> = values().toList()
        fun fromDisplayName(displayName: String): Posicion? {
            return values().find { it.displayName == displayName }
        }
    }
}