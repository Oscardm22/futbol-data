package com.example.futboldata.ui.equipos.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.futboldata.R
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.model.Posicion
import com.example.futboldata.databinding.DialogAddJugadorBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class AddJugadorDialog : DialogFragment() {

    private var _binding: DialogAddJugadorBinding? = null
    private val binding get() = _binding!!

    private var onJugadorAdded: ((Jugador) -> Unit)? = null

    fun setOnJugadorAddedListener(listener: (Jugador) -> Unit) {
        onJugadorAdded = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddJugadorBinding.inflate(layoutInflater)

        setupUI()

        return BottomSheetDialog(requireContext()).apply {
            setContentView(binding.root)
            setCancelable(true)

            // Configurar comportamiento del botón guardar
            binding.btnSave.setOnClickListener {
                guardarJugador()
            }
        }
    }

    private fun setupUI() {
        setupPosicionSpinner()
        setupFocusListeners()
    }

    private fun setupPosicionSpinner() {
        binding.spinnerPosicion.setOnClickListener {
            binding.spinnerPosicion.showDropDown()
        }

        val posiciones = Posicion.entries.map { it.name }
        val posicionAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, posiciones)
        binding.spinnerPosicion.setAdapter(posicionAdapter)
    }

    private fun setupFocusListeners() {
        listOf(binding.etNombre, binding.spinnerPosicion).forEach { view ->
            view.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    clearErrorForView(view)
                }
            }
        }
    }

    private fun guardarJugador() {
        if (!validarFormulario()) return

        try {
            val jugador = crearJugadorDesdeFormulario()
            onJugadorAdded?.invoke(jugador)
            dismiss()
        } catch (e: Exception) {
            showError("Error: ${e.localizedMessage}")
        }
    }

    private fun validarFormulario(): Boolean {
        var isValid = true

        // Validar nombre
        if (binding.etNombre.text.isNullOrBlank()) {
            binding.tilNombre.error = getString(R.string.error_campo_obligatorio)
            isValid = false
        } else {
            binding.tilNombre.error = null
        }

        // Validar posición
        if (binding.spinnerPosicion.text.isNullOrBlank()) {
            binding.tilPosicion.error = "Selecciona una posición"
            isValid = false
        } else {
            binding.tilPosicion.error = null
        }

        return isValid
    }

    private fun crearJugadorDesdeFormulario(): Jugador {
        val equipoId = arguments?.getString(ARG_EQUIPO_ID)
            ?: throw IllegalStateException("Equipo ID requerido")

        return Jugador(
            nombre = binding.etNombre.text.toString(),
            posicion = Posicion.valueOf(binding.spinnerPosicion.text.toString()),
            equipoId = equipoId
        )
    }

    private fun clearErrorForView(view: View) {
        when (view) {
            binding.etNombre -> binding.tilNombre.error = null
            binding.spinnerPosicion -> binding.tilPosicion.error = null
        }
    }

    private fun showError(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_EQUIPO_ID = "equipo_id"

        fun newInstance(equipoId: String): AddJugadorDialog {
            return AddJugadorDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_EQUIPO_ID, equipoId)
                }
            }
        }
    }
}