package com.example.futboldata.ui.equipos.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.adapter.JugadoresAlineacionAdapter
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.model.ParticipacionJugador
import com.example.futboldata.databinding.FragmentAlineacionBinding

class AlineacionFragment : Fragment() {
    private var _binding: FragmentAlineacionBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: JugadoresAlineacionAdapter
    private val seleccionados = mutableListOf<ParticipacionJugador>()
    private var _jugadores: List<Jugador> = emptyList()

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
            val participacion = ParticipacionJugador(
                jugadorId = jugador.id,
                esTitular = esTitular,
                goles = 0,
                asistencias = 0,
                minutosJugados = if (esTitular) 90 else 0,
                tarjetasAmarillas = 0,
                tarjetasRojas = 0
            )

            seleccionados.removeAll { it.jugadorId == jugador.id }
            seleccionados.add(participacion)
        }

        binding.recyclerViewAlineacion.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AlineacionFragment.adapter
        }
    }

    fun updateJugadores(jugadores: List<Jugador>) {
        _jugadores = jugadores
        if (::adapter.isInitialized) {
            // Actualizar las selecciones en el adapter
            adapter.setSelecciones(seleccionados)
            adapter.submitList(jugadores.toList())
        }
    }

    fun getAlineacionSeleccionada(): List<ParticipacionJugador> {
        return seleccionados.toList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}