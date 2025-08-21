package com.example.futboldata.ui.equipos.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.adapter.GoleadoresAdapter
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.FragmentGoleadoresBinding

class GoleadoresFragment : Fragment() {
    private var _binding: FragmentGoleadoresBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GoleadoresAdapter
    private val golesMap = mutableMapOf<String, Int>() // Cambiado a Map
    private var _jugadores: List<Jugador> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGoleadoresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = GoleadoresAdapter(
            onGolAdded = { jugadorId, jugadorNombre ->
                val golesActuales = golesMap[jugadorId] ?: 0
                golesMap[jugadorId] = golesActuales + 1
                adapter.updateGoles(golesMap)
            },
            onGolRemoved = { jugadorId, jugadorNombre ->
                val golesActuales = golesMap[jugadorId] ?: 0
                if (golesActuales > 0) {
                    golesMap[jugadorId] = golesActuales - 1
                    adapter.updateGoles(golesMap)
                }
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@GoleadoresFragment.adapter
        }

        if (_jugadores.isNotEmpty()) {
            adapter.submitList(_jugadores.toList())
            adapter.updateGoles(golesMap)
        }
    }

    fun updateJugadores(jugadores: List<Jugador>) {
        Log.d("DEBUG_FRAGMENT", "updateJugadores - ${this::class.simpleName} - Jugadores: ${jugadores.size}")
        _jugadores = jugadores
        if (::adapter.isInitialized) {
            adapter.submitList(jugadores.toList())
            adapter.updateGoles(golesMap)
        }
    }

    fun getGoleadores(): Map<String, Int> {
        return golesMap.toMap()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}