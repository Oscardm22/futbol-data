package com.example.futboldata.ui.equipos.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
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

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Añadir gol")
            .setView(dialogBinding.root)
            .setPositiveButton("Añadir") { _, _ ->
                val minuto = dialogBinding.etMinuto.text.toString().toIntOrNull() ?: run {
                    dialogBinding.etMinuto.error = "Minuto inválido"
                    return@setPositiveButton
                }

                val esPenalti = dialogBinding.cbPenalti.isChecked

                val gol = Gol(
                    jugadorId = jugadorId,
                    jugadorNombre = jugadorNombre,
                    minuto = minuto,
                    tipo = if (esPenalti) "Penalti" else "Normal"
                )

                golesRegistrados.add(gol)

                adapter.updateGoles(golesRegistrados)

            }
            .setNegativeButton("Cancelar", null)
            .show()
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