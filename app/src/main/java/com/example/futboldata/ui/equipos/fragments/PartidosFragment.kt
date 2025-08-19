package com.example.futboldata.ui.equipos.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.adapter.PartidosAdapter
import com.example.futboldata.databinding.FragmentPartidosBinding
import com.example.futboldata.viewmodel.EquipoDetailViewModel

class PartidosFragment : Fragment() {
    private var _binding: FragmentPartidosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EquipoDetailViewModel by activityViewModels()

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

        // Observador de los partidos y competiciones
        viewModel.equipo.observe(viewLifecycleOwner) { equipo ->
            viewModel.partidos.observe(viewLifecycleOwner) { partidos ->
                viewModel.competiciones.observe(viewLifecycleOwner) { competiciones ->
                    partidos?.let { partidosList ->
                        // ORDENAR los partidos por fecha
                        val partidosOrdenados = partidosList.sortedByDescending { it.fecha }

                        // Crear mapa de imágenes de competición
                        val imagesMap = competiciones?.associate { it.id to it.imagenBase64 } ?: emptyMap()

                        // Crear mapa de nombres de equipos (solo para el equipo actual)
                        val teamNamesMap = equipo?.let { mapOf(it.id to it.nombre) } ?: emptyMap()

                        // Crear mapa de nombres de competiciones
                        val competitionNamesMap = competiciones?.associate { it.id to it.nombre } ?: emptyMap()

                        // Crear mapa de tipos de competición
                        val competitionTypesMap = competiciones?.associate { it.id to it.tipo } ?: emptyMap()

                        binding.rvPartidos.adapter = PartidosAdapter(
                            matches = partidosOrdenados, // Usar la lista ordenada
                            competitionImages = imagesMap,
                            teamNames = teamNamesMap,
                            competitionNames = competitionNamesMap,
                            competitionTypes = competitionTypesMap
                        )
                    }
                }
            }
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