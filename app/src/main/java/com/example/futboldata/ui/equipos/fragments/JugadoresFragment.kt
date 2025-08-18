package com.example.futboldata.ui.equipos.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.R
import com.example.futboldata.adapter.JugadoresAdapter
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.FragmentJugadoresBinding
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
        adapter = JugadoresAdapter { jugador ->
            showDeleteConfirmationDialog(jugador)
        }

        binding.rvJugadores.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@JugadoresFragment.adapter
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

    private fun showDeleteConfirmationDialog(jugador: Jugador) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_dialog_title_player))
            .setMessage(getString(R.string.delete_dialog_message_player, jugador.nombre))
            .setPositiveButton(getString(R.string.delete_button)) { _, _ ->
                viewModel.eliminarJugador(jugador)
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