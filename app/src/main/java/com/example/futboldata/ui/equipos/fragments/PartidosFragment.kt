package com.example.futboldata.ui.equipos.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.R
import com.example.futboldata.adapter.JugadorSimpleAdapter
import com.example.futboldata.adapter.PartidosAdapter
import com.example.futboldata.data.model.Equipo
import com.example.futboldata.data.model.Partido
import com.example.futboldata.data.model.Posicion
import com.example.futboldata.data.model.TipoCompeticion
import com.example.futboldata.databinding.DialogPartidoDetalleBinding
import com.example.futboldata.databinding.FragmentPartidosBinding
import com.example.futboldata.viewmodel.EquipoDetailViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.Locale

class PartidosFragment : Fragment() {
    private var _binding: FragmentPartidosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EquipoDetailViewModel by activityViewModels()
    private val nombresJugadores = mutableMapOf<String, String>()
    private val posicionesJugadores = mutableMapOf<String, Posicion>() // Map para posiciones

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPartidosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración del RecyclerView
        binding.rvPartidos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        // Observador del equipo para cargar jugadores
        viewModel.equipo.observe(viewLifecycleOwner) { equipo ->
            equipo?.let {
                // Cargar jugadores del equipo
                viewModel.cargarJugadores(it.id)

                // Observar los jugadores
                viewModel.jugadores.observe(viewLifecycleOwner) { jugadores ->
                    jugadores?.forEach { jugador ->
                        nombresJugadores[jugador.id] = jugador.nombre
                        posicionesJugadores[jugador.id] = jugador.posicion // Guardar posición
                    }

                    // Una vez cargados los jugadores, cargar partidos y competiciones
                    cargarPartidosYCompeticiones(equipo)
                }
            }
        }
    }

    private fun cargarPartidosYCompeticiones(equipo: Equipo) {
        viewModel.partidos.observe(viewLifecycleOwner) { partidos ->
            viewModel.competiciones.observe(viewLifecycleOwner) { competiciones ->
                partidos?.let { partidosList ->
                    // ORDENAR los partidos por fecha
                    val partidosOrdenados = partidosList.sortedByDescending { it.fecha }

                    // Crear mapa de imágenes de competición
                    val imagesMap =
                        competiciones?.associate { it.id to it.imagenBase64 } ?: emptyMap()

                    // Crear mapa de nombres de equipos (solo para el equipo actual)
                    val teamNamesMap = mapOf(equipo.id to equipo.nombre)

                    // Crear mapa de nombres de competiciones
                    val competitionNamesMap =
                        competiciones?.associate { it.id to it.nombre } ?: emptyMap()

                    // Crear mapa de tipos de competición
                    val competitionTypesMap =
                        competiciones?.associate { it.id to it.tipo } ?: emptyMap()

                    binding.rvPartidos.adapter = PartidosAdapter(
                        matches = partidosOrdenados,
                        competitionImages = imagesMap,
                        teamNames = teamNamesMap,
                        competitionNames = competitionNamesMap,
                        competitionTypes = competitionTypesMap,
                        onPartidoClickListener = { partido ->
                            mostrarBottomSheetPartido(partido, equipo.nombre)
                        }
                    )
                }
            }
        }
    }

    private fun mostrarBottomSheetPartido(partido: Partido, nombreEquipo: String) {
        // Crear el BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bindingSheet = DialogPartidoDetalleBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bindingSheet.root)

        // Obtener el mapa de tipos de competición
        val competitionTypesMap = viewModel.competiciones.value?.associate { it.id to it.tipo } ?: emptyMap()

        // Configurar la UI del BottomSheet
        configurarBottomSheetUI(bindingSheet, partido, nombreEquipo, competitionTypesMap)

        // Mostrar el diálogo
        bottomSheetDialog.show()
    }

    private fun configurarBottomSheetUI(
        binding: DialogPartidoDetalleBinding,
        partido: Partido,
        nombreEquipo: String,
        competitionTypesMap: Map<String, TipoCompeticion>
    ) {
        // Configurar marcador y equipos
        if (partido.esLocal) {
            binding.tvEquipoLocal.text = getString(R.string.equipo_local, nombreEquipo)
            binding.tvEquipoVisitante.text = partido.rival
            binding.tvMarcador.text = getString(R.string.marcador_template, partido.golesEquipo, partido.golesRival)
        } else {
            binding.tvEquipoLocal.text = partido.rival
            binding.tvEquipoVisitante.text = getString(R.string.equipo_visitante, nombreEquipo)
            binding.tvMarcador.text = getString(R.string.marcador_template, partido.golesRival, partido.golesEquipo)
        }

        // Configurar fecha
        val dateFormat = SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.getDefault())
        binding.tvFecha.text = dateFormat.format(partido.fecha)

        // Configurar competición
        binding.tvCompeticion.text = partido.competicionNombre

        // Obtener tipo de competición del mapa
        val tipoCompeticion = competitionTypesMap[partido.competicionId] ?: TipoCompeticion.LIGA

        // Configurar jornada o fase según el tipo de competición
        binding.tvJornadaFase.visibility = View.VISIBLE

        when (tipoCompeticion) {
            TipoCompeticion.LIGA -> {
                partido.jornada?.let {
                    binding.tvJornadaFase.text = getString(R.string.jornada_template, it)
                } ?: run {
                    binding.tvJornadaFase.visibility = View.GONE
                }
            }
            TipoCompeticion.COPA_NACIONAL,
            TipoCompeticion.COPA_INTERNACIONAL,
            TipoCompeticion.SUPERCOPA -> {
                partido.fase?.let {
                    binding.tvJornadaFase.text = it
                } ?: run {
                    binding.tvJornadaFase.visibility = View.GONE
                }
            }
        }

        // Calcular cantidad de goles y asistencias por jugador
        val golesPorJugador = partido.goleadoresIds.groupingBy { it }.eachCount()
        val asistenciasPorJugador = partido.asistentesIds.groupingBy { it }.eachCount()

        // ORDENAR la alineación por posición
        val ordenPosiciones = listOf(
            Posicion.PO,    // 1. Porteros
            Posicion.DFC,   // 2. Defensas centrales
            Posicion.LD,    // 3. Laterales derechos
            Posicion.LI,    // 4. Laterales izquierdos
            Posicion.MCD,   // 5. Mediocentros defensivos
            Posicion.MC,    // 6. Mediocentros
            Posicion.MCO,   // 7. Mediocentros ofensivos
            Posicion.MD,    // 8. Mediocentros derechos
            Posicion.MI,    // 9. Mediocentros izquierdos
            Posicion.ED,    // 10. Extremos derechos
            Posicion.EI,    // 11. Extremos izquierdos
            Posicion.DC     // 12. Delanteros centros
        )

        val alineacion = partido.alineacionIds.map { id ->
            JugadorSimpleAdapter.JugadorAlineacion(
                id = id,
                cantidadGoles = golesPorJugador[id] ?: 0,
                cantidadAsistencias = asistenciasPorJugador[id] ?: 0,
                esMvp = partido.jugadorDelPartido == id,
                esPorteroImbatido = partido.porteroImbatidoId == id
            )
        }.sortedBy { jugador ->
            val posicion = posicionesJugadores[jugador.id]
            ordenPosiciones.indexOf(posicion).takeIf { it != -1 } ?: ordenPosiciones.size
        }

        binding.rvAlineacion.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = JugadorSimpleAdapter(alineacion, nombresJugadores, posicionesJugadores)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PartidosFragment()
    }
}