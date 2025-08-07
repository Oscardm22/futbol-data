package com.example.futboldata.ui.equipos.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.R
import com.example.futboldata.adapter.AsistenciasAdapter
import com.example.futboldata.data.model.Asistencia
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.FragmentAsistenciasBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.futboldata.databinding.DialogAddAsistenciaBinding

class AsistenciasFragment : Fragment() {
    private var _binding: FragmentAsistenciasBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AsistenciasAdapter
    private val asistenciasRegistradas = mutableListOf<Asistencia>()
    private var _jugadores: List<Jugador> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAsistenciasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AsistenciasAdapter { jugadorId, jugadorNombre ->
            showAsistenciaDialog(jugadorId, jugadorNombre)
        }

        binding.rvAsistencias.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AsistenciasFragment.adapter
        }

        if (_jugadores.isNotEmpty()) {
            adapter.submitList(_jugadores.toList())
        }
    }


    private fun showAsistenciaDialog(jugadorId: String, jugadorNombre: String) {
        val dialogBinding = DialogAddAsistenciaBinding.inflate(layoutInflater)
        var dialog: AlertDialog? = null

        // Configurar el TextInputLayout para mostrar errores
        dialogBinding.tilMinuto.errorIconDrawable = null

        // Limpiar error cuando el usuario escribe
        dialogBinding.etMinuto.doAfterTextChanged {
            dialogBinding.tilMinuto.error = null
            dialogBinding.tilMinuto.isErrorEnabled = false
        }

        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Añadir asistencia")
            .setView(dialogBinding.root)
            .setPositiveButton("Añadir", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            // Cambiar colores de los botones
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.botones_positivos))
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.Fondo))

            positiveButton.setOnClickListener {
                val minutoTexto = dialogBinding.etMinuto.text.toString().trim()

                // Validación en cascada con mensajes específicos
                when {
                    minutoTexto.isBlank() -> {
                        dialogBinding.tilMinuto.error = getString(R.string.error_minuto_vacio)
                        dialogBinding.tilMinuto.isErrorEnabled = true
                    }
                    !minutoTexto.matches(Regex("\\d+")) -> {
                        dialogBinding.tilMinuto.error = getString(R.string.error_minuto_invalido)
                        dialogBinding.tilMinuto.isErrorEnabled = true
                    }
                    minutoTexto.toInt() !in 1..120 -> {
                        dialogBinding.tilMinuto.error = getString(R.string.error_minuto_rango)
                        dialogBinding.tilMinuto.isErrorEnabled = true
                    }
                    else -> {
                        val asistencia = Asistencia(
                            jugadorId = jugadorId,
                            jugadorNombre = jugadorNombre,
                            minuto = minutoTexto.toInt()
                        )
                        asistenciasRegistradas.add(asistencia)
                        adapter.updateAsistencias(asistenciasRegistradas)
                        dialog.dismiss()
                    }
                }
            }
        }

        dialogBinding.etMinuto.requestFocus()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        dialog.show()
    }

    fun updateJugadores(jugadores: List<Jugador>) {
        Log.d("DEBUG_FRAGMENT", "updateJugadores - ${this::class.simpleName} - Jugadores: ${jugadores.size}")
        _jugadores = jugadores
        if (::adapter.isInitialized) {
            Log.d("DEBUG_ADAPTER", "Enviando lista al adapter")
            adapter.submitList(jugadores.toList())
        }
    }

    fun getAsistencias(): List<Asistencia> {
        return asistenciasRegistradas.toList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}