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

        // Configurar la UI del BottomSheet
        configurarBottomSheetUI(bindingSheet, partido, nombreEquipo)

        // Mostrar el diálogo
        bottomSheetDialog.show()
    }

    private fun configurarBottomSheetUI(
        binding: DialogPartidoDetalleBinding,
        partido: Partido,
        nombreEquipo: String
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

        // Calcular cantidad de goles y asistencias por jugador
        val golesPorJugador = partido.goleadoresIds.groupingBy { it }.eachCount()
        val asistenciasPorJugador = partido.asistentesIds.groupingBy { it }.eachCount()

        // Configurar la alineación en el RecyclerView
        val alineacion = partido.alineacionIds.map { id ->
            JugadorSimpleAdapter.JugadorAlineacion(
                id = id,
                cantidadGoles = golesPorJugador[id] ?: 0,
                cantidadAsistencias = asistenciasPorJugador[id] ?: 0,
                esMvp = partido.jugadorDelPartido == id,
            )
        }

        binding.rvAlineacion.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = JugadorSimpleAdapter(alineacion, nombresJugadores)
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