package com.example.futboldata.ui.equipos.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.R
import com.example.futboldata.adapter.DestacadosAdapter
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.model.Posicion
import com.example.futboldata.databinding.FragmentDestacadosBinding
import com.example.futboldata.viewmodel.EquipoDetailViewModel

class DestacadosFragment : Fragment() {

    private var _binding: FragmentDestacadosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EquipoDetailViewModel by activityViewModels()

    companion object {
        fun newInstance(): DestacadosFragment {
            return DestacadosFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDestacadosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = DestacadosAdapter()
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.jugadores.observe(viewLifecycleOwner) { jugadores ->
            jugadores?.let {
                val destacados = obtenerEstadisticasDestacadas(it)
                // Actualizar RecyclerView con los datos destacados
                (binding.recyclerView.adapter as? DestacadosAdapter)?.submitList(destacados)
            }
        }

        viewModel.partidos.observe(viewLifecycleOwner) { partidos ->
        }
    }

    private fun obtenerEstadisticasDestacadas(jugadores: List<Jugador>): List<EstadisticaDestacada> {
        val destacados = mutableListOf<EstadisticaDestacada>()

        // Top 5 Goleadores
        val topGoleadores = jugadores.filter { it.goles > 0 }
            .sortedByDescending { it.goles }
            .take(5)

        if (topGoleadores.isNotEmpty()) {
            destacados.add(EstadisticaDestacada(
                tipo = "TOP 5 GOLEADORES",
                jugador = "", // Vacío para el header
                valor = "",
                icono = R.drawable.ic_goal,
                esHeader = true
            ))
            topGoleadores.forEachIndexed { index, jugador ->
                destacados.add(EstadisticaDestacada(
                    tipo = "${index + 1}.",
                    jugador = jugador.nombre,
                    valor = "${jugador.goles} goles",
                    icono = 0,
                    esHeader = false
                ))
            }
        }

        // Top 5 Asistentes
        val topAsistentes = jugadores.filter { it.asistencias > 0 }
            .sortedByDescending { it.asistencias }
            .take(5)

        if (topAsistentes.isNotEmpty()) {
            destacados.add(EstadisticaDestacada(
                tipo = "TOP 5 ASISTENTES",
                jugador = "",
                valor = "",
                icono = R.drawable.ic_assist,
                esHeader = true
            ))
            topAsistentes.forEachIndexed { index, jugador ->
                destacados.add(EstadisticaDestacada(
                    tipo = "${index + 1}.",
                    jugador = jugador.nombre,
                    valor = "${jugador.asistencias} asistencias",
                    icono = 0,
                    esHeader = false
                ))
            }
        }

        // Top 5 MVP
        val topMVP = jugadores.filter { it.mvp > 0 }
            .sortedByDescending { it.mvp }
            .take(5)

        if (topMVP.isNotEmpty()) {
            destacados.add(EstadisticaDestacada(
                tipo = "TOP 5 MVP",
                jugador = "",
                valor = "",
                icono = R.drawable.ic_mvp_selected,
                esHeader = true
            ))
            topMVP.forEachIndexed { index, jugador ->
                destacados.add(EstadisticaDestacada(
                    tipo = "${index + 1}.",
                    jugador = jugador.nombre,
                    valor = "${jugador.mvp} MVP",
                    icono = 0,
                    esHeader = false
                ))
            }
        }

        // Top 5 Partidos Jugados
        val topPartidos = jugadores.filter { it.partidosJugados > 0 }
            .sortedByDescending { it.partidosJugados }
            .take(5)

        if (topPartidos.isNotEmpty()) {
            destacados.add(EstadisticaDestacada(
                tipo = "TOP 5 PARTIDOS",
                jugador = "",
                valor = "",
                icono = R.drawable.ic_match,
                esHeader = true
            ))
            topPartidos.forEachIndexed { index, jugador ->
                destacados.add(EstadisticaDestacada(
                    tipo = "${index + 1}.",
                    jugador = jugador.nombre,
                    valor = "${jugador.partidosJugados} partidos",
                    icono = 0,
                    esHeader = false
                ))
            }
        }

        // Top 5 Porteros Imbatidos (solo porteros)
        val topPorteros = jugadores.filter { it.posicion == Posicion.PO && it.porteriasImbatidas > 0 }
            .sortedByDescending { it.porteriasImbatidas }
            .take(5)

        if (topPorteros.isNotEmpty()) {
            destacados.add(EstadisticaDestacada(
                tipo = "TOP 5 PORTERÍAS IMBATIDAS",
                jugador = "",
                valor = "",
                icono = R.drawable.ic_goalkeeper,
                esHeader = true
            ))
            topPorteros.forEachIndexed { index, jugador ->
                destacados.add(EstadisticaDestacada(
                    tipo = "${index + 1}.",
                    jugador = jugador.nombre,
                    valor = "${jugador.porteriasImbatidas} porterías",
                    icono = 0,
                    esHeader = false
                ))
            }
        }

        return destacados
    }
}

data class EstadisticaDestacada(
    val tipo: String,
    val jugador: String,
    val valor: String,
    val icono: Int,
    val esHeader: Boolean = false
)