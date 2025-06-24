package com.example.futboldata.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.futboldata.data.repository.EquipoRepository

class EquipoViewModelFactory(
    private val repository: EquipoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EquipoViewModel::class.java)) {
            return EquipoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}