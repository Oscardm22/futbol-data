package com.example.futboldata.ui.equipos.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.adapter.CompeticionAdapter
import com.example.futboldata.databinding.DialogSeleccionarCompeticionBinding
import com.example.futboldata.viewmodel.EquipoDetailViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class FiltroCompeticionDialog : DialogFragment() {

    private var _binding: DialogSeleccionarCompeticionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EquipoDetailViewModel by activityViewModels()

    private var onCompeticionSelected: ((competicion: com.example.futboldata.data.model.Competicion?) -> Unit)? = null

    fun setOnCompeticionSelectedListener(listener: (com.example.futboldata.data.model.Competicion?) -> Unit) {
        onCompeticionSelected = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogSeleccionarCompeticionBinding.inflate(layoutInflater)

        setupRecyclerView()
        setupClickListeners()
        setupObservers()

        return BottomSheetDialog(requireContext()).apply {
            setContentView(binding.root)
            setCancelable(true)
        }
    }

    private fun setupRecyclerView() {
        binding.rvCompeticiones.layoutManager = LinearLayoutManager(requireContext())

        val adapter = CompeticionAdapter(
            onItemClick = { competicion ->
                onCompeticionSelected?.invoke(competicion)
                dismiss()
            },
            onDeleteClick = {
                // No hacer nada en modo filtro
            },
            modoFiltro = true // Activar modo filtro para ocultar botón de eliminar
        )

        binding.rvCompeticiones.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnTodas.setOnClickListener {
            onCompeticionSelected?.invoke(null)
            dismiss()
        }
    }

    private fun setupObservers() {
        viewModel.competiciones.observe(this) { competiciones ->
            val adapter = binding.rvCompeticiones.adapter as? CompeticionAdapter
            adapter?.updateList(competiciones)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): FiltroCompeticionDialog {
            return FiltroCompeticionDialog()
        }
    }
}