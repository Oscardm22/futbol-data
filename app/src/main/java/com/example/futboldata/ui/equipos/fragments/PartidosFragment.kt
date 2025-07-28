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

        // Observador de los partidos
        viewModel.partidos.observe(viewLifecycleOwner) { partidos ->
            partidos?.let {
                binding.rvPartidos.adapter = PartidosAdapter(it)
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