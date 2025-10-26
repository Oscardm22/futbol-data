package com.example.futboldata.ui.equipos.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.futboldata.databinding.DialogJugadoresPartidoBinding
import com.example.futboldata.ui.equipos.fragments.AlineacionFragment
import com.example.futboldata.ui.equipos.fragments.AsistenciasFragment
import com.example.futboldata.ui.equipos.fragments.GoleadoresFragment
import com.example.futboldata.ui.equipos.fragments.MVPFragment
import com.example.futboldata.viewmodel.EquipoDetailViewModel
import com.google.android.material.tabs.TabLayoutMediator
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog

class JugadoresPartidoDialog : DialogFragment() {

    private var _binding: DialogJugadoresPartidoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EquipoDetailViewModel by activityViewModels()

    private var equipoId: String? = null
    private var golesEquipoInput: Int = 0
    private var autogolesFavor: Int = 0

    // Callbacks
    private var onAlineacionSelected: ((List<String>) -> Unit)? = null
    private var onGoleadoresSelected: ((Map<String, Int>) -> Unit)? = null
    private var onAsistenciasSelected: ((Map<String, Int>) -> Unit)? = null
    private var onMvpSelected: ((String?) -> Unit)? = null

    private lateinit var alineacionFragment: AlineacionFragment
    private lateinit var goleadoresFragment: GoleadoresFragment
    private lateinit var asistenciasFragment: AsistenciasFragment
    private lateinit var mvpFragment: MVPFragment

    fun setEquipoId(equipoId: String) {
        this.equipoId = equipoId
    }

    fun setGolesEquipoInput(goles: Int) {
        this.golesEquipoInput = goles
    }

    fun setAutogolesFavor(autogoles: Int) {
        this.autogolesFavor = autogoles
    }

    fun setOnJugadoresSelectedListener(
        onAlineacionSelected: (List<String>) -> Unit,
        onGoleadoresSelected: (Map<String, Int>) -> Unit,
        onAsistenciasSelected: (Map<String, Int>) -> Unit,
        onMvpSelected: (String?) -> Unit
    ) {
        this.onAlineacionSelected = onAlineacionSelected
        this.onGoleadoresSelected = onGoleadoresSelected
        this.onAsistenciasSelected = onAsistenciasSelected
        this.onMvpSelected = onMvpSelected
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogJugadoresPartidoBinding.inflate(layoutInflater)

        // CREAR LOS FRAGMENTS DIRECTAMENTE - COMO EN TU CÓDIGO ORIGINAL
        alineacionFragment = AlineacionFragment()
        goleadoresFragment = GoleadoresFragment()
        asistenciasFragment = AsistenciasFragment()
        mvpFragment = MVPFragment()

        setupViewPager()
        setupClickListeners()
        cargarJugadores()

        return BottomSheetDialog(requireContext()).apply {
            setContentView(binding.root)
            setCancelable(true)
        }
    }

    private fun setupViewPager() {
        val adapter = JugadoresPartidoPagerAdapter(this).apply {
            addFragment(alineacionFragment, "Alineación")
            addFragment(goleadoresFragment, "Goles")
            addFragment(asistenciasFragment, "Asistencias")
            addFragment(mvpFragment, "MVP")
        }

        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getTitle(position)
        }.attach()
    }

    private fun setupClickListeners() {
        binding.btnConfirm.setOnClickListener {
            validarYConfirmar()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun cargarJugadores() {
        equipoId?.let { id ->
            viewModel.cargarJugadores(id)

            viewModel.jugadores.observe(this) { jugadores ->
                if (jugadores.isNotEmpty()) {
                    alineacionFragment.updateJugadores(jugadores)
                    goleadoresFragment.updateJugadores(jugadores)
                    asistenciasFragment.updateJugadores(jugadores)
                    mvpFragment.updateJugadores(jugadores)
                }
            }
        }
    }


    private fun validarYConfirmar() {
        val alineacion = alineacionFragment.getAlineacionSeleccionada()
        val goleadores = goleadoresFragment.getGoleadores()
        val asistencias = asistenciasFragment.getAsistencias()
        val mvp = mvpFragment.getMVP()

        // Validaciones
        if (!realizarValidaciones(alineacion, goleadores, asistencias, mvp)) {
            return
        }

        // Si pasa todas las validaciones, ejecutar callbacks
        onAlineacionSelected?.invoke(alineacion)
        onGoleadoresSelected?.invoke(goleadores)
        onAsistenciasSelected?.invoke(asistencias)
        onMvpSelected?.invoke(mvp)

        dismiss()
    }

    private fun realizarValidaciones(
        alineacion: List<String>,
        goleadores: Map<String, Int>,
        asistencias: Map<String, Int>,
        mvp: String?
    ): Boolean {
        // 1. Validación de alineación - Mínimo 11 titulares
        if (alineacion.size < 11) {
            showError("Debes seleccionar al menos 11 titulares")
            return false
        }

        // 2. Validación de alineación - Máximo 14 jugadores
        if (alineacion.size > 14) {
            showError("Máximo 14 jugadores en la alineación")
            return false
        }

        // 3. Calcular totales
        val totalGolesRegistrados = goleadores.values.sum()
        val totalAsistenciasRegistradas = asistencias.values.sum()
        val golesTotales = totalGolesRegistrados + autogolesFavor

        // 4. Validación de consistencia de goles
        if (golesTotales != golesEquipoInput) {
            showError(
                "Error: Registraste $golesTotales goles (jugadores: $totalGolesRegistrados + " +
                        "autogoles: $autogolesFavor) pero ingresaste $golesEquipoInput goles en el marcador"
            )
            return false
        }

        // 5. Validación de consistencia de asistencias
        if (totalAsistenciasRegistradas > totalGolesRegistrados) {
            showError(
                "Error: No puede haber más asistencias ($totalAsistenciasRegistradas) " +
                        "que goles de jugadores ($totalGolesRegistrados)"
            )
            return false
        }

        // 6. Validación: jugadores no en alineación no pueden tener goles
        val jugadoresConGoles = goleadores.filter { it.value > 0 }.keys
        val jugadoresConGolesNoAlineados = jugadoresConGoles - alineacion.toSet()

        if (jugadoresConGolesNoAlineados.isNotEmpty()) {
            showError("Error: Los jugadores con goles tienen que estar en la alineación")
            return false
        }

        // 7. Validación: jugadores no en alineación no pueden tener asistencias
        val jugadoresConAsistencias = asistencias.filter { it.value > 0 }.keys
        val jugadoresConAsistenciasNoAlineados = jugadoresConAsistencias - alineacion.toSet()

        if (jugadoresConAsistenciasNoAlineados.isNotEmpty()) {
            showError("Error: Los jugadores con asistencias tienen que estar en la alineación")
            return false
        }

        // 8. Validación: MVP debe estar en alineación
        if (mvp != null && !alineacion.contains(mvp)) {
            showError("Error: El MVP debe estar en la alineación")
            return false
        }

        return true
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Pager Adapter interno
    inner class JugadoresPartidoPagerAdapter(fragment: DialogFragment) :
        androidx.viewpager2.adapter.FragmentStateAdapter(fragment) {

        private val fragments = mutableListOf<Pair<Fragment, String>>()

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(Pair(fragment, title))
        }

        fun getTitle(position: Int): String = fragments[position].second

        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment = fragments[position].first
    }
}