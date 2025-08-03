package com.example.futboldata.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Competicion(
    val id: String = "",
    val nombre: String = "",
    val tipo: TipoCompeticion,
    val imagenBase64: String = ""
) : Parcelable {

    fun getIniciales(): String {
        return nombre.split(" ")
            .take(2).joinToString("") { it.firstOrNull()?.toString() ?: "" }
            .uppercase()
    }

    constructor() : this("", "", TipoCompeticion.LIGA, "")
}
