package com.example.futboldata.ui.equipos.fragments

import android.os.Bundle
import android.util.Log
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
        Log.d("DEBUG_FRAGMENT", "Configurando RecyclerView")
        adapter = JugadoresAdapter()
        binding.rvJugadores.apply {
            layoutManager = LinearLayoutManager(requireContext()).also {
                Log.d("DEBUG_FRAGMENT", "LayoutManager configurado")
            }
            adapter = this@JugadoresFragment.adapter.also {
                Log.d("DEBUG_FRAGMENT", "Adapter asignado al RecyclerView")
            }
        }
    }

    private fun setupObservers() {
        viewModel.jugadores.observe(viewLifecycleOwner) { jugadores ->
            Log.d("DEBUG_FRAGMENT", "Nuevos datos recibidos. Primer jugador: ${jugadores.firstOrNull()?.nombre}")
            adapter.submitList(jugadores) {
                Log.d("DEBUG_FRAGMENT", "submitList completado. Lista tamaño: ${adapter.itemCount}")
                binding.rvJugadores.post {
                    Log.d("DEBUG_FRAGMENT", "RecyclerView estado: width=${binding.rvJugadores.width}, height=${binding.rvJugadores.height}, visibility=${binding.rvJugadores.visibility}")
                }
            }
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