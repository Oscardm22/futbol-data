package com.example.futboldata.utils

import com.example.futboldata.utils.TeamValidator.ValidationResult.Invalid
import com.example.futboldata.utils.TeamValidator.ValidationResult.Valid

object TeamValidator {

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val errorMessage: String) : ValidationResult()
    }

    fun validateTeamName(name: String): ValidationResult {
        return when {
            name.isEmpty() -> Invalid("El nombre no puede estar vacío")
            name.length < 3 -> Invalid("El nombre debe tener al menos 3 caracteres")
            name.length > 30 -> Invalid("El nombre no puede exceder 30 caracteres")
            else -> Valid
        }
    }
}