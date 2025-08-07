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
import com.example.futboldata.adapter.GoleadoresAdapter
import com.example.futboldata.data.model.Gol
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.FragmentGoleadoresBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.futboldata.databinding.DialogAddGolBinding

class GoleadoresFragment : Fragment() {
    private var _binding: FragmentGoleadoresBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GoleadoresAdapter
    private val golesRegistrados = mutableListOf<Gol>()
    private var _jugadores: List<Jugador> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGoleadoresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = GoleadoresAdapter { jugadorId, jugadorNombre ->
            showGolDialog(jugadorId, jugadorNombre)
        }

        if (_jugadores.isNotEmpty()) {
            adapter.submitList(_jugadores.toList())
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@GoleadoresFragment.adapter
        }
    }

    private fun showGolDialog(jugadorId: String, jugadorNombre: String) {
        val dialogBinding = DialogAddGolBinding.inflate(layoutInflater)
        var dialog: AlertDialog? = null

        // Configurar el TextInputLayout para mostrar errores
        dialogBinding.tilMinuto.errorIconDrawable = null

        // Limpiar error cuando el usuario escribe
        dialogBinding.etMinuto.doAfterTextChanged {
            dialogBinding.tilMinuto.error = null
            dialogBinding.tilMinuto.isErrorEnabled = false
        }

        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Añadir gol")
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
                        val esPenalti = dialogBinding.cbPenalti.isChecked
                        val gol = Gol(
                            jugadorId = jugadorId,
                            jugadorNombre = jugadorNombre,
                            minuto = minutoTexto.toInt(),
                            tipo = if (esPenalti) "Penalti" else "Normal"
                        )
                        golesRegistrados.add(gol)
                        adapter.updateGoles(golesRegistrados)
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
            adapter.submitList(jugadores.toList())
        }
    }

    fun getGoleadores(): List<Gol> {
        return golesRegistrados.toList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}