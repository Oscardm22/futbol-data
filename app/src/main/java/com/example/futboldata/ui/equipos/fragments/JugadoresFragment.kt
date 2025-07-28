package com.example.futboldata.ui.equipos.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.adapter.JugadoresAdapter
import com.example.futboldata.databinding.FragmentJugadoresBinding
import com.example.futboldata.viewmodel.EquipoDetailViewModel

class JugadoresFragment : Fragment() {
    private var _binding: FragmentJugadoresBinding? = null
    private val binding get() = _binding!!

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

        // Configura el RecyclerView
        binding.rvJugadores.layoutManager = LinearLayoutManager(requireContext())

        // Observa los cambios en la lista de jugadores
        viewModel.jugadores.observe(viewLifecycleOwner) { jugadores ->
            binding.rvJugadores.adapter = JugadoresAdapter(jugadores)
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