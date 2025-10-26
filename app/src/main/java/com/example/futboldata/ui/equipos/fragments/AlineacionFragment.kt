package com.example.futboldata.ui.equipos.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.adapter.JugadoresAlineacionAdapter
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.FragmentAlineacionBinding
import com.example.futboldata.utils.JugadorUtils

class AlineacionFragment : Fragment() {
    private var _binding: FragmentAlineacionBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: JugadoresAlineacionAdapter
    private val jugadoresSeleccionados = mutableMapOf<String, Boolean>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlineacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = JugadoresAlineacionAdapter { jugador, esTitular ->
            // Actualizamos el mapa de seleccionados
            jugadoresSeleccionados[jugador.id] = esTitular
        }

        binding.recyclerViewAlineacion.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AlineacionFragment.adapter
        }
    }

    fun updateJugadores(jugadores: List<Jugador>) {
        if (::adapter.isInitialized) {
            val jugadoresOrdenados = JugadorUtils.ordenarJugadoresPorPosicion(jugadores)
            adapter.setSeleccionesIniciales(jugadoresSeleccionados.keys.toList())
            adapter.submitList(jugadoresOrdenados)
        }
    }

    fun getAlineacionSeleccionada(): List<String> {
        // Devuelve solo los IDs de los jugadores titulares
        return jugadoresSeleccionados.filter { it.value }.keys.toList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}