package com.example.futboldata.ui.equipos.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.adapter.AsistenciasAdapter
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.FragmentAsistenciasBinding
import com.example.futboldata.utils.JugadorUtils

class AsistenciasFragment : Fragment() {
    private var _binding: FragmentAsistenciasBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AsistenciasAdapter
    private val asistenciasMap = mutableMapOf<String, Int>()
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

        adapter = AsistenciasAdapter(
            onAsistenciaAdded = { jugadorId, jugadorNombre ->
                val asistenciasActuales = asistenciasMap[jugadorId] ?: 0
                asistenciasMap[jugadorId] = asistenciasActuales + 1
                adapter.updateAsistencias(asistenciasMap)
            },
            onAsistenciaRemoved = { jugadorId, jugadorNombre ->
                val asistenciasActuales = asistenciasMap[jugadorId] ?: 0
                if (asistenciasActuales > 0) {
                    asistenciasMap[jugadorId] = asistenciasActuales - 1
                    adapter.updateAsistencias(asistenciasMap)
                }
            }
        )

        binding.rvAsistencias.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AsistenciasFragment.adapter
        }

        if (_jugadores.isNotEmpty()) {
            adapter.submitList(_jugadores.toList())
            adapter.updateAsistencias(asistenciasMap)
        }
    }

    fun updateJugadores(jugadores: List<Jugador>) {
        Log.d("DEBUG_FRAGMENT", "updateJugadores - ${this::class.simpleName} - Jugadores: ${jugadores.size}")
        val jugadoresOrdenados = JugadorUtils.ordenarJugadoresPorPosicion(jugadores)
        _jugadores = jugadoresOrdenados
        if (::adapter.isInitialized) {
            Log.d("DEBUG_ADAPTER", "Enviando lista al adapter")
            adapter.submitList(jugadoresOrdenados.toList())
            adapter.updateAsistencias(asistenciasMap)
        }
    }

    fun getAsistencias(): Map<String, Int> {
        return asistenciasMap.toMap()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}