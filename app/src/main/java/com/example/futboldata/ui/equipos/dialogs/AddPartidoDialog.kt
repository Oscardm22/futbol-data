package com.example.futboldata.ui.equipos.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.futboldata.R
import com.example.futboldata.data.model.Partido
import com.example.futboldata.databinding.DialogAddPartidoBinding
import com.example.futboldata.viewmodel.EquipoDetailViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*

class AddPartidoDialog : DialogFragment() {

    private var _binding: DialogAddPartidoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EquipoDetailViewModel by activityViewModels()

    private var onPartidoAdded: ((Partido) -> Unit)? = null
    private val competicionMap = mutableMapOf<String, String>()

    // Estados del diálogo
    private var alineacionSeleccionada = mutableListOf<String>()
    private var goleadoresMap = mutableMapOf<String, Int>()
    private var asistenciasMap = mutableMapOf<String, Int>()
    private var jugadorDelPartido: String? = null

    fun setOnPartidoAddedListener(listener: (Partido) -> Unit) {
        onPartidoAdded = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddPartidoBinding.inflate(layoutInflater)

        setupUI()

        return BottomSheetDialog(requireContext()).apply {
            setContentView(binding.root)
            setCancelable(true)
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

            // Configurar comportamiento del botón guardar
            binding.btnSave.setOnClickListener {
                guardarPartido()
            }
        }
    }

    private fun setupUI() {
        setupCompeticionSpinner()
        setupFocusListeners()
        setupJugadoresClickListener()
    }

    private fun setupJugadoresClickListener() {
        binding.btnAddJugadores.setOnClickListener {
            abrirDialogoJugadoresPartido()
        }
    }

    private fun setupCompeticionSpinner() {
        binding.spinnerCompeticion.setOnClickListener {
            binding.spinnerCompeticion.showDropDown()
        }

        viewModel.competiciones.observe(this) { competiciones ->
            competiciones?.let {
                competicionMap.clear()
                it.forEach { comp -> competicionMap[comp.nombre] = comp.id }

                val competicionAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.dropdown_item,
                    it.map { comp -> comp.nombre }
                )
                binding.spinnerCompeticion.setAdapter(competicionAdapter)
            }
        }
    }

    private fun setupFocusListeners() {
        listOf(
            binding.etRival,
            binding.etGolesEquipo,
            binding.etGolesRival,
            binding.etTemporada,
            binding.spinnerCompeticion,
            binding.etAutogolesFavor
        ).forEach { view ->
            view.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    clearErrorForView(view)
                }
            }
        }
    }

    private fun abrirDialogoJugadoresPartido() {
        val equipoId = arguments?.getString(ARG_EQUIPO_ID) ?: return

        val golesEquipoInput = binding.etGolesEquipo.text.toString().toIntOrNull() ?: 0
        val autogolesFavor = binding.etAutogolesFavor.text.toString().toIntOrNull() ?: 0

        val jugadoresDialog = JugadoresPartidoDialog().apply {
            setEquipoId(equipoId)
            setGolesEquipoInput(golesEquipoInput)
            setAutogolesFavor(autogolesFavor)
            setOnJugadoresSelectedListener(
                onAlineacionSelected = { alineacion ->
                    alineacionSeleccionada = alineacion.toMutableList()
                },
                onGoleadoresSelected = { goleadores ->
                    goleadoresMap = goleadores.toMutableMap()
                },
                onAsistenciasSelected = { asistencias ->
                    asistenciasMap = asistencias.toMutableMap()
                },
                onMvpSelected = { mvp ->
                    jugadorDelPartido = mvp
                }
            )
        }

        jugadoresDialog.show(parentFragmentManager, "JugadoresPartidoDialog")
    }

    private fun guardarPartido() {
        if (!validarFormulario()) return

        try {
            val partido = crearPartidoDesdeFormulario()
            onPartidoAdded?.invoke(partido)
            dismiss()
        } catch (e: Exception) {
            showError("Error: ${e.localizedMessage}")
        }
    }

    private fun validarFormulario(): Boolean {
        var isValid = true

        with(binding) {
            // Validar campos obligatorios
            if (etRival.text.isNullOrBlank()) {
                tilRival.error = getString(R.string.error_campo_obligatorio)
                isValid = false
            }

            if (etGolesEquipo.text.toString().toIntOrNull() == null) {
                tilGolesEquipo.error = "Valor inválido"
                isValid = false
            }

            if (etGolesRival.text.toString().toIntOrNull() == null) {
                tilGolesRival.error = "Valor inválido"
                isValid = false
            }

            if (etTemporada.text.isNullOrBlank()) {
                tilTemporada.error = "La temporada es obligatoria"
                isValid = false
            }

            if (spinnerCompeticion.text.isNullOrBlank()) {
                tilCompeticion.error = "Selecciona una competición"
                isValid = false
            }
        }

        return isValid
    }

    private fun crearPartidoDesdeFormulario(): Partido {
        val equipoId = arguments?.getString(ARG_EQUIPO_ID) ?: throw IllegalStateException("Equipo ID requerido")

        with(binding) {
            val competicionNombre = spinnerCompeticion.text.toString()
            val competicionId = competicionMap[competicionNombre]
                ?: throw IllegalArgumentException("Competición no válida")

            // Validaciones finales de consistencia
            validarConsistenciaEstadisticas()

            return Partido(
                equipoId = equipoId,
                fecha = Date(),
                rival = etRival.text.toString(),
                golesEquipo = etGolesEquipo.text.toString().toInt(),
                golesRival = etGolesRival.text.toString().toInt(),
                competicionId = competicionId,
                competicionNombre = competicionNombre,
                temporada = etTemporada.text.toString(),
                fase = etFase.text.toString().takeIf { it.isNotBlank() },
                jornada = etJornada.text.toString().toIntOrNull(),
                esLocal = switchLocal.isChecked,
                alineacionIds = alineacionSeleccionada,
                goleadoresIds = goleadoresMap.flatMap { (id, cant) -> List(cant) { id } },
                asistentesIds = asistenciasMap.flatMap { (id, cant) -> List(cant) { id } },
                jugadorDelPartido = jugadorDelPartido,
                autogolesFavor = etAutogolesFavor.text.toString().toIntOrNull() ?: 0
            )
        }
    }

    private fun validarConsistenciaEstadisticas() {
        val totalGoles = goleadoresMap.values.sum()
        val totalAsistencias = asistenciasMap.values.sum()
        val autogolesFavor = binding.etAutogolesFavor.text.toString().toIntOrNull() ?: 0
        val golesTotales = totalGoles + autogolesFavor
        val golesEquipoInput = binding.etGolesEquipo.text.toString().toIntOrNull() ?: 0

        if (golesTotales != golesEquipoInput) {
            throw IllegalArgumentException(
                "Goles totales ($golesTotales) no coinciden con marcador. " +
                        "Incluye $totalGoles goles de jugadores + $autogolesFavor autogoles"
            )
        }

        if (totalAsistencias > totalGoles) {
            throw IllegalArgumentException("No puede haber más asistencias que goles de jugadores")
        }

        if (alineacionSeleccionada.size < 11) {
            throw IllegalArgumentException("Debes seleccionar al menos 11 jugadores en la alineación")
        }
    }

    private fun clearErrorForView(view: View) {
        with(binding) {
            when (view) {
                etRival -> tilRival.error = null
                etGolesEquipo -> tilGolesEquipo.error = null
                etGolesRival -> tilGolesRival.error = null
                etTemporada -> tilTemporada.error = null
                spinnerCompeticion -> tilCompeticion.error = null
                etAutogolesFavor -> tilAutogolesFavor.error = null
            }
        }
    }

    private fun showError(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_EQUIPO_ID = "equipo_id"

        fun newInstance(equipoId: String): AddPartidoDialog {
            return AddPartidoDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_EQUIPO_ID, equipoId)
                }
            }
        }
    }
}