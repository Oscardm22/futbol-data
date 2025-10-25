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
    constructor() : this("", "", TipoCompeticion.LIGA, "")
}