package com.example.futboldata.ui.equipos.fragments

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.R
import com.example.futboldata.adapter.DestacadosAdapter
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.model.Posicion
import com.example.futboldata.databinding.FragmentDestacadosBinding
import com.example.futboldata.viewmodel.EquipoDetailViewModel
import com.example.futboldata.data.model.Competicion
import kotlinx.coroutines.launch

class DestacadosFragment : Fragment() {

    private var _binding: FragmentDestacadosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EquipoDetailViewModel by activityViewModels()
    private lateinit var adapter: DestacadosAdapter
    private var todosLosJugadores: List<Jugador> = emptyList() // ← Almacena TODOS los jugadores

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
        cargarTodosLosJugadores() // ← NUEVO: Cargar todos los jugadores
    }

    private fun cargarTodosLosJugadores() {
        lifecycleScope.launch {
            try {
                val equipoId = viewModel.equipo.value?.id
                equipoId?.let {
                    // Obtener todos los jugadores del equipo (sin filtrar por activo)
                    todosLosJugadores = viewModel.getTodosLosJugadoresPorEquipo(it)
                    actualizarDestacados()
                }
            } catch (e: Exception) {
                // En caso de error, usar los jugadores activos como fallback
                todosLosJugadores = viewModel.jugadores.value ?: emptyList()
                actualizarDestacados()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = DestacadosAdapter(
            onJugadorClick = { nombreJugador ->
                // Buscar el jugador por nombre en TODOS los jugadores
                todosLosJugadores.find { it.nombre == nombreJugador }?.let { jugador ->
                    showPlayerDetails(jugador)
                }
            }
        )
        binding.recyclerView.adapter = adapter
    }

    private fun showPlayerDetails(jugador: Jugador) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(jugador.nombre)
            .setView(createPlayerDetailsView(jugador))
            .create()

        dialog.show()
    }

    private fun createPlayerDetailsView(jugador: Jugador): View {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_player_details, binding.root, false)

        // Configurar todas las estadísticas usando recursos de string
        view.findViewById<TextView>(R.id.tvPosicion).text = getString(R.string.posicion, jugador.posicion.toString())
        view.findViewById<TextView>(R.id.tvPartidos).text = getString(R.string.partidos, jugador.partidosJugados)
        view.findViewById<TextView>(R.id.tvGoles).text = getString(R.string.goles, jugador.goles)
        view.findViewById<TextView>(R.id.tvAsistencias).text = getString(R.string.asistencias, jugador.asistencias)
        view.findViewById<TextView>(R.id.tvMVP).text = getString(R.string.mvp, jugador.mvp)
        view.findViewById<TextView>(R.id.tvPorterias).text = getString(R.string.porterias_imbatidas, jugador.porteriasImbatidas)

        // Mostrar estadísticas por competición
        setupCompetitionStats(view, jugador)

        return view
    }

    private fun setupCompetitionStats(view: View, jugador: Jugador) {
        val container = view.findViewById<LinearLayout>(R.id.containerStatsCompeticion)
        container.removeAllViews()

        // Verificar que el mapa no sea null antes de iterar
        jugador.golesPorCompeticion.forEach { (compId, goles) ->
            val statView = LayoutInflater.from(view.context)
                .inflate(R.layout.item_stat_competicion, container, false)

            // Usar el ViewModel para obtener el nombre de la competición
            val compName = viewModel.getCompetitionName(compId)
            statView.findViewById<TextView>(R.id.tvCompeticion).text = compName

            // Usar recursos de string para las estadísticas
            statView.findViewById<TextView>(R.id.tvGoles).text = getString(R.string.competicion_goles, goles)
            statView.findViewById<TextView>(R.id.tvAsistencias).text = getString(
                R.string.competicion_asistencias,
                jugador.asistenciasPorCompeticion[compId] ?: 0
            )
            statView.findViewById<TextView>(R.id.tvMVP).text = getString(
                R.string.competicion_mvp,
                jugador.mvpPorCompeticion[compId] ?: 0
            )
            statView.findViewById<TextView>(R.id.tvPartidos).text = getString(
                R.string.competicion_partidos,
                jugador.partidosPorCompeticion[compId] ?: 0
            )

            container.addView(statView)
        }

        // Mensaje si no hay estadísticas por competición
        if (jugador.golesPorCompeticion.isEmpty()) {
            val emptyView = TextView(view.context).apply {
                text = getString(R.string.no_estadisticas_competicion)
                setTextAppearance(android.R.style.TextAppearance_Medium)
                setPadding(0, 16.dpToPx(view.context), 0, 0)
            }
            container.addView(emptyView)
        }
    }

    private fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

    private fun setupObservers() {
        // Observar cambios en la competición seleccionada
        viewModel.competicionSeleccionada.observe(viewLifecycleOwner) { competicion ->
            competicion?.let {
                binding.tvTituloCompeticion.text = getString(R.string.competicion_formato, it.nombre)
                binding.tvTituloCompeticion.visibility = View.VISIBLE
                actualizarDestacados()
            } ?: run {
                binding.tvTituloCompeticion.visibility = View.GONE
                actualizarDestacados()
            }
        }

        viewModel.jugadores.observe(viewLifecycleOwner) { jugadoresActivos ->
            // Actualizar la lista de todos los jugadores si hay cambios
            if (todosLosJugadores.isEmpty()) {
                todosLosJugadores = jugadoresActivos ?: emptyList()
                actualizarDestacados()
            }
        }
    }

    private fun actualizarDestacados() {
        val competicionId = viewModel.competicionSeleccionada.value?.id
        val destacados = obtenerEstadisticasDestacadas(todosLosJugadores, competicionId)
        adapter.submitList(destacados)
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
                icono = R.drawable.ic_games,
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
    }
}

data class EstadisticaDestacada(
    val tipo: String,
    val jugador: String,
    val valor: String,
    val icono: Int,
    val esHeader: Boolean = false
)