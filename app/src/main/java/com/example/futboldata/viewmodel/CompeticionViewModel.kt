package com.example.futboldata.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futboldata.data.managers.CompeticionManager
import com.example.futboldata.data.model.Competicion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CompeticionViewModel(
    private val manager: CompeticionManager
) : ViewModel() {
    private val _competiciones = MutableStateFlow<List<Competicion>>(emptyList())
    val competiciones: StateFlow<List<Competicion>> = _competiciones

    init {
        loadCompeticiones()
    }

    private fun loadCompeticiones() {
        viewModelScope.launch {
            _competiciones.value = manager.getCompeticionesActivas()
        }
    }
}