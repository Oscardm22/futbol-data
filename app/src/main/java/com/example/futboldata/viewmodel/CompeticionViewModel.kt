package com.example.futboldata.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futboldata.data.model.Competicion
import com.example.futboldata.data.repository.CompeticionRepository
import kotlinx.coroutines.launch

class CompeticionViewModel(
    private val repository: CompeticionRepository
) : ViewModel() {

    private val _competiciones = MutableLiveData<List<Competicion>>()
    val competiciones: LiveData<List<Competicion>> = _competiciones

    private val _operacionState = MutableLiveData<OperacionState>()
    val operacionState: LiveData<OperacionState> = _operacionState

    sealed class OperacionState {
        object Loading : OperacionState()
        data class Success(val mensaje: String) : OperacionState()
        data class Error(val mensaje: String) : OperacionState()
    }

    init {
        cargarCompeticiones()
    }

    fun cargarCompeticiones() {
        viewModelScope.launch {
            _operacionState.value = OperacionState.Loading
            try {
                _competiciones.value = repository.getCompeticiones()
                _operacionState.value = OperacionState.Success("Datos cargados")
            } catch (e: Exception) {
                _operacionState.value = OperacionState.Error("Error al cargar competiciones: ${e.message}")
            }
        }
    }

    fun crearCompeticion(competicion: Competicion) {
        viewModelScope.launch {
            _operacionState.value = OperacionState.Loading
            try {
                repository.saveCompeticion(competicion)
                cargarCompeticiones() // Recargamos la lista después de crear
                _operacionState.value = OperacionState.Success("Competición creada exitosamente")
            } catch (e: Exception) {
                _operacionState.value = OperacionState.Error("Error al crear competición: ${e.message}")
            }
        }
    }

    fun actualizarCompeticion(competicion: Competicion) {
        viewModelScope.launch {
            _operacionState.value = OperacionState.Loading
            try {
                repository.updateCompeticion(competicion)
                cargarCompeticiones() // Recargar la lista después de actualizar
                _operacionState.value = OperacionState.Success("Competición actualizada")
            } catch (e: Exception) {
                _operacionState.value = OperacionState.Error("Error al actualizar: ${e.message}")
            }
        }
    }

    fun eliminarCompeticion(id: String) {
        viewModelScope.launch {
            _operacionState.value = OperacionState.Loading
            try {
                repository.deleteCompeticion(id)
                cargarCompeticiones()
                _operacionState.value = OperacionState.Success("Competición eliminada")
            } catch (e: Exception) {
                _operacionState.value = OperacionState.Error("Error al eliminar: ${e.message}")
            }
        }
    }
}