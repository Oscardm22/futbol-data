package com.example.futboldata.ui.equipos.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.R
import com.example.futboldata.adapter.JugadoresAdapter
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.FragmentJugadoresBinding
import com.example.futboldata.utils.JugadorUtils
import com.example.futboldata.viewmodel.EquipoDetailViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class JugadoresFragment : Fragment() {
    private var _binding: FragmentJugadoresBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: JugadoresAdapter

    private val viewModel: EquipoDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJugadoresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DEBUG_UI", "▶ [Fragment] Inicializando JugadoresFragment")

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = JugadoresAdapter(
            onDeleteClick = { jugador ->
                showDeleteConfirmationDialog(jugador)
            },
            onPlayerClick = { jugador ->
                showPlayerDetails(jugador)
            }
        )

        binding.rvJugadores.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@JugadoresFragment.adapter
        }
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

    private fun setupObservers() {
        viewModel.jugadores.observe(viewLifecycleOwner) { jugadores ->
            val jugadoresOrdenados = JugadorUtils.ordenarJugadoresPorPosicion(jugadores)

            Log.d("DEBUG_FRAGMENT", "Nuevos datos recibidos. Primer jugador: ${jugadoresOrdenados.firstOrNull()?.nombre} (Posición: ${jugadoresOrdenados.firstOrNull()?.posicion?.displayName})")

            // 3. Enviar lista ordenada al adapter
            adapter.submitList(jugadoresOrdenados) {
                Log.d("DEBUG_FRAGMENT", "submitList completado. Lista tamaño: ${adapter.itemCount}")
                Log.d("DEBUG_ORDER", "Orden actual: ${jugadoresOrdenados.joinToString { "${it.posicion.displayName} - ${it.nombre}" }}")

                binding.rvJugadores.post {
                    Log.d("DEBUG_FRAGMENT", "RecyclerView estado: width=${binding.rvJugadores.width}, height=${binding.rvJugadores.height}, visibility=${binding.rvJugadores.visibility}")
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(jugador: Jugador) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_dialog_title_player))
            .setMessage(getString(R.string.delete_dialog_message_player, jugador.nombre))
            .setPositiveButton(getString(R.string.delete_button)) { _, _ ->
                viewModel.desactivarJugador(jugador)
            }
            .setNegativeButton(getString(R.string.cancel_button), null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.error_color)
                    )
                    getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.Fondo)
                    )
                }
                show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = JugadoresFragment()
    }
}

fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()