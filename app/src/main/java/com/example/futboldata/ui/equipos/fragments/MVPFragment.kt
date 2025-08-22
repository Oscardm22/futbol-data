package com.example.futboldata.ui.equipos.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.adapter.MVPAdapter
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.FragmentMvpBinding
import com.example.futboldata.utils.JugadorUtils

class MVPFragment : Fragment() {
    private var _binding: FragmentMvpBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MVPAdapter
    private var jugadorSeleccionado: String? = null
    private var _jugadores: List<Jugador> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMvpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MVPAdapter { jugadorId ->
            jugadorSeleccionado = jugadorId
        }

        binding.rvJugadores.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MVPFragment.adapter
        }

        if (_jugadores.isNotEmpty()) {
            adapter.submitList(_jugadores.toList())
        }
    }

    fun updateJugadores(jugadores: List<Jugador>) {
        Log.d("DEBUG_FRAGMENT", "updateJugadores - ${this::class.simpleName} - Jugadores: ${jugadores.size}")
        val jugadoresOrdenados = JugadorUtils.ordenarJugadoresPorPosicion(jugadores)
        _jugadores = jugadoresOrdenados
        if (::adapter.isInitialized) {
            Log.d("DEBUG_ADAPTER", "Enviando lista al adapter")
            adapter.submitList(jugadoresOrdenados.toList())

            // Restaurar selección si existe
            jugadorSeleccionado?.let { selectedId ->
                if (jugadoresOrdenados.any { it.id == selectedId }) {
                    adapter.setSelectedJugador(selectedId)
                } else {
                    jugadorSeleccionado = null
                }
            }
        }
    }

    fun getMVP(): String? {
        return jugadorSeleccionado
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}