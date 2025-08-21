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
import com.example.futboldata.data.model.Competicion

class DestacadosFragment : Fragment() {

    private var _binding: FragmentDestacadosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EquipoDetailViewModel by activityViewModels()
    private lateinit var adapter: DestacadosAdapter

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
        adapter = DestacadosAdapter()
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.jugadores.observe(viewLifecycleOwner) { jugadores ->
            jugadores?.let {
                val competicionId = viewModel.competicionSeleccionada.value?.id
                val destacados = obtenerEstadisticasDestacadas(it, competicionId)
                adapter.submitList(destacados)
            }
        }

        viewModel.competicionSeleccionada.observe(viewLifecycleOwner) { competicion ->
            competicion?.let {
                binding.tvTituloCompeticion.text = getString(R.string.competicion_formato, it.nombre)
                binding.tvTituloCompeticion.visibility = View.VISIBLE

                // Recargar estadísticas cuando cambia la competición
                viewModel.jugadores.value?.let { jugadores ->
                    val destacados = obtenerEstadisticasDestacadas(jugadores, it.id)
                    adapter.submitList(destacados)
                }
            } ?: run {
                binding.tvTituloCompeticion.visibility = View.GONE

                // Mostrar todas las competiciones
                viewModel.jugadores.value?.let { jugadores ->
                    val destacados = obtenerEstadisticasDestacadas(jugadores, null)
                    adapter.submitList(destacados)
                }
            }
        }
    }

    private fun obtenerEstadisticasDestacadas(
        jugadores: List<Jugador>,
        competicionId: String?
    ): List<EstadisticaDestacada> {
        val destacados = mutableListOf<EstadisticaDestacada>()

        // Función helper para obtener el valor según la competición
        fun obtenerValor(jugador: Jugador, campo: String): Int {
            return if (competicionId != null) {
                // Obtener valor específico de la competición
                when (campo) {
                    "goles" -> jugador.golesPorCompeticion[competicionId] ?: 0
                    "asistencias" -> jugador.asistenciasPorCompeticion[competicionId] ?: 0
                    "mvp" -> jugador.mvpPorCompeticion[competicionId] ?: 0
                    "partidos" -> jugador.partidosPorCompeticion[competicionId] ?: 0
                    "porterias" -> jugador.porteriasImbatidasPorCompeticion[competicionId] ?: 0
                    else -> 0
                }
            } else {
                // Obtener valor total
                when (campo) {
                    "goles" -> jugador.goles
                    "asistencias" -> jugador.asistencias
                    "mvp" -> jugador.mvp
                    "partidos" -> jugador.partidosJugados
                    "porterias" -> jugador.porteriasImbatidas
                    else -> 0
                }
            }
        }

        // Top 5 Goleadores
        val topGoleadores = jugadores
            .sortedByDescending { obtenerValor(it, "goles") }
            .take(5)
            .filter { obtenerValor(it, "goles") > 0 }

        if (topGoleadores.isNotEmpty()) {
            destacados.add(EstadisticaDestacada(
                tipo = "TOP 5 GOLEADORES",
                jugador = "",
                valor = "",
                icono = R.drawable.ic_goal,
                esHeader = true
            ))
            topGoleadores.forEachIndexed { index, jugador ->
                val goles = obtenerValor(jugador, "goles")
                destacados.add(EstadisticaDestacada(
                    tipo = "${index + 1}.",
                    jugador = jugador.nombre,
                    valor = "$goles goles",
                    icono = 0,
                    esHeader = false
                ))
            }
        }

        // Top 5 Asistentes
        val topAsistentes = jugadores
            .sortedByDescending { obtenerValor(it, "asistencias") }
            .take(5)
            .filter { obtenerValor(it, "asistencias") > 0 }

        if (topAsistentes.isNotEmpty()) {
            destacados.add(EstadisticaDestacada(
                tipo = "TOP 5 ASISTENTES",
                jugador = "",
                valor = "",
                icono = R.drawable.ic_assist,
                esHeader = true
            ))
            topAsistentes.forEachIndexed { index, jugador ->
                val asistencias = obtenerValor(jugador, "asistencias")
                destacados.add(EstadisticaDestacada(
                    tipo = "${index + 1}.",
                    jugador = jugador.nombre,
                    valor = "$asistencias asistencias",
                    icono = 0,
                    esHeader = false
                ))
            }
        }

        // Top 5 MVP
        val topMVP = jugadores
            .sortedByDescending { obtenerValor(it, "mvp") }
            .take(5)
            .filter { obtenerValor(it, "mvp") > 0 }

        if (topMVP.isNotEmpty()) {
            destacados.add(EstadisticaDestacada(
                tipo = "TOP 5 MVP",
                jugador = "",
                valor = "",
                icono = R.drawable.ic_mvp_selected,
                esHeader = true
            ))
            topMVP.forEachIndexed { index, jugador ->
                val mvp = obtenerValor(jugador, "mvp")
                destacados.add(EstadisticaDestacada(
                    tipo = "${index + 1}.",
                    jugador = jugador.nombre,
                    valor = "$mvp MVP",
                    icono = 0,
                    esHeader = false
                ))
            }
        }

        // Top 5 Partidos Jugados
        val topPartidos = jugadores
            .sortedByDescending { obtenerValor(it, "partidos") }
            .take(5)
            .filter { obtenerValor(it, "partidos") > 0 }

        if (topPartidos.isNotEmpty()) {
            destacados.add(EstadisticaDestacada(
                tipo = "TOP 5 PARTIDOS",
                jugador = "",
                valor = "",
                icono = R.drawable.ic_match,
                esHeader = true
            ))
            topPartidos.forEachIndexed { index, jugador ->
                val partidos = obtenerValor(jugador, "partidos")
                destacados.add(EstadisticaDestacada(
                    tipo = "${index + 1}.",
                    jugador = jugador.nombre,
                    valor = "$partidos partidos",
                    icono = 0,
                    esHeader = false
                ))
            }
        }

        // Top 5 Porteros Imbatidos
        val topPorteros = jugadores
            .filter { it.posicion == Posicion.PO}
            .sortedByDescending { obtenerValor(it, "porterias") }
            .take(5)
            .filter { obtenerValor(it, "porterias") > 0 }

        if (topPorteros.isNotEmpty()) {
            destacados.add(EstadisticaDestacada(
                tipo = "TOP 5 PORTERÍAS IMBATIDAS",
                jugador = "",
                valor = "",
                icono = R.drawable.ic_goalkeeper,
                esHeader = true
            ))
            topPorteros.forEachIndexed { index, jugador ->
                val porterias = obtenerValor(jugador, "porterias")
                destacados.add(EstadisticaDestacada(
                    tipo = "${index + 1}.",
                    jugador = jugador.nombre,
                    valor = "$porterias porterías",
                    icono = 0,
                    esHeader = false
                ))
            }
        }

        return destacados
    }

    fun filtrarPorCompeticion(competicion: Competicion?) {
        viewModel.seleccionarCompeticion(competicion)

        viewModel.jugadores.value?.let { jugadores ->
            val competicionId = competicion?.id
            val destacados = obtenerEstadisticasDestacadas(jugadores, competicionId)
            adapter.submitList(destacados)
        }
    }
}

data class EstadisticaDestacada(
    val tipo: String,
    val jugador: String,
    val valor: String,
    val icono: Int,
    val esHeader: Boolean = false
)