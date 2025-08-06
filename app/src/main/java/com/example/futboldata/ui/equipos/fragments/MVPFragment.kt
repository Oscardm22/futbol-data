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
            adapter.setSelectedJugador(jugadorId)
        }

        binding.rvJugadores.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MVPFragment.adapter
        }
    }

    fun updateJugadores(jugadores: List<Jugador>) {
        Log.d("DEBUG_FRAGMENT", "updateJugadores - ${this::class.simpleName} - Jugadores: ${jugadores.size}")
        _jugadores = jugadores
        if (::adapter.isInitialized) {
            Log.d("DEBUG_ADAPTER", "Enviando lista al adapter")
            adapter.submitList(jugadores.toList())
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