package com.example.futboldata.ui.equipos

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.futboldata.databinding.ActivityEquipoDetailBinding
import com.example.futboldata.data.repository.impl.EquipoRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import com.example.futboldata.utils.StatsCalculator
import com.example.futboldata.R
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.model.Partido
import com.example.futboldata.ui.equipos.fragments.JugadoresFragment
import com.example.futboldata.ui.equipos.fragments.PartidosFragment
import com.example.futboldata.ui.equipos.fragments.StatsFragment
import com.example.futboldata.viewmodel.EquipoDetailViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.futboldata.adapter.CompeticionAdapter
import com.example.futboldata.data.model.Posicion
import com.example.futboldata.data.repository.impl.CompeticionRepositoryImpl
import com.example.futboldata.data.repository.impl.JugadorRepositoryImpl
import com.example.futboldata.data.repository.impl.PartidoRepositoryImpl
import com.example.futboldata.databinding.DialogAddJugadorBinding
import com.example.futboldata.databinding.DialogAddPartidoBinding
import com.example.futboldata.databinding.DialogJugadoresPartidoBinding
import com.example.futboldata.databinding.DialogSeleccionarCompeticionBinding
import com.example.futboldata.ui.equipos.fragments.AlineacionFragment
import com.example.futboldata.ui.equipos.fragments.AsistenciasFragment
import com.example.futboldata.ui.equipos.fragments.DestacadosFragment
import com.example.futboldata.ui.equipos.fragments.GoleadoresFragment
import com.example.futboldata.ui.equipos.fragments.MVPFragment
import java.util.Date

open class EquipoDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEquipoDetailBinding
    protected lateinit var viewModel: EquipoDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEquipoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val equipoId = intent.getStringExtra("equipo_id") ?: run {
            finish()
            return
        }

        setupViewModel()
        setupViewPager()
        setupFabBehavior()
        setupObservers()
        viewModel.cargarEquipo(equipoId)
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(StatsFragment.newInstance(), getString(R.string.tab_estadisticas))
        adapter.addFragment(PartidosFragment.newInstance(), getString(R.string.tab_partidos))
        adapter.addFragment(JugadoresFragment.newInstance(), getString(R.string.tab_jugadores))
        adapter.addFragment(DestacadosFragment.newInstance(), getString(R.string.tab_destacados))

        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getTitle(position)
        }.attach()
    }

    inner class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        val fragments = mutableListOf<Pair<Fragment, String>>()

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(Pair(fragment, title))
        }

        fun getTitle(position: Int): String = fragments[position].second

        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position].first
    }

    private fun setupFabBehavior() {
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> binding.fabAddMatch.hide()
                    1 -> showFabForPartidos()
                    2 -> showFabForJugadores()
                    3 -> showFabForDestacados()
                }
            }
        })
    }

    private fun showFabForPartidos() {
        binding.fabAddMatch.apply {
            setImageResource(R.drawable.ic_add_match)
            setOnClickListener { showAddPartidoDialog() }
            show()
        }
    }

    private fun showFabForJugadores() {
        binding.fabAddMatch.apply {
            setImageResource(R.drawable.ic_add_player)
            setOnClickListener { showAddJugadorDialog() }
            show()
        }
    }

    private fun showFabForDestacados() {
        binding.fabAddMatch.apply {
            setImageResource(R.drawable.ic_filter)
            setOnClickListener {
                // Mostrar diálogo de filtrado por competición
                mostrarDialogoFiltroCompeticion()
            }
            show()
        }
    }

    private fun mostrarDialogoFiltroCompeticion() {
        val dialog = BottomSheetDialog(this)
        val binding = DialogSeleccionarCompeticionBinding.inflate(layoutInflater)

        val rvCompeticiones = binding.rvCompeticiones
        val btnTodas = binding.btnTodas

        rvCompeticiones.layoutManager = LinearLayoutManager(this)

        val adapter = CompeticionAdapter(
            competiciones = emptyList(),
            onItemClick = { competicion ->
                val destacadosFragment = getDestacadosFragment()
                destacadosFragment?.filtrarPorCompeticion(competicion)
                dialog.dismiss()
            },
            onDeleteClick = {
                // No hacer nada en modo filtro
            },
            modoFiltro = true // Activar modo filtro para ocultar botón de eliminar
        )

        btnTodas.setOnClickListener {
            val destacadosFragment = getDestacadosFragment()
            destacadosFragment?.filtrarPorCompeticion(null)
            dialog.dismiss()
        }

        // Cargar competiciones
        viewModel.competiciones.observe(this) { competiciones ->
            adapter.updateList(competiciones)
        }

        rvCompeticiones.adapter = adapter
        dialog.setContentView(binding.root)
        dialog.show()
    }

    private fun getDestacadosFragment(): DestacadosFragment? {
        return supportFragmentManager.findFragmentByTag("f${binding.viewPager.currentItem}") as? DestacadosFragment
    }

    private fun showJugadoresPartidoDialog(
        equipoId: String,
        golesEquipoInput: Int,
        onAlineacionSelected: (List<String>) -> Unit,
        onGoleadoresSelected: (Map<String, Int>) -> Unit,
        onAsistenciasSelected: (Map<String, Int>) -> Unit,
        onMvpSelected: (String?) -> Unit
    ) {
        val dialog = BottomSheetDialog(this)
        val binding = DialogJugadoresPartidoBinding.inflate(layoutInflater)

        val alineacionFragment = AlineacionFragment()
        val goleadoresFragment = GoleadoresFragment()
        val asistenciasFragment = AsistenciasFragment()
        val mvpFragment = MVPFragment()

        val adapter = ViewPagerAdapter(this).apply {
            addFragment(alineacionFragment, "Alineación")
            addFragment(goleadoresFragment, "Goles")
            addFragment(asistenciasFragment, "Asistencias")
            addFragment(mvpFragment, "MVP")
        }

        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getTitle(position)
        }.attach()

        viewModel.jugadores.observe(this) { jugadores ->
            if (jugadores.isNotEmpty()) {
                alineacionFragment.updateJugadores(jugadores)
                goleadoresFragment.updateJugadores(jugadores)
                asistenciasFragment.updateJugadores(jugadores)
                mvpFragment.updateJugadores(jugadores)
            }
        }

        viewModel.cargarJugadores(equipoId)

        binding.btnConfirm.setOnClickListener {
            val alineacion = alineacionFragment.getAlineacionSeleccionada()
            val goleadores = goleadoresFragment.getGoleadores()
            val asistencias = asistenciasFragment.getAsistencias()
            val mvp = mvpFragment.getMVP()

            // 1. VALIDACIÓN DE ALINEACIÓN - Mínimo 11 titulares
            if (alineacion.size < 11) {
                Toast.makeText(this, "Debes seleccionar al menos 11 titulares", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. VALIDACIÓN DE ALINEACIÓN - Máximo 14 jugadores
            if (alineacion.size > 14) {
                Toast.makeText(this, "Máximo 14 jugadores en la alineación", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. CALCULAR TOTALES
            val totalGolesRegistrados = goleadores.values.sum()
            val totalAsistenciasRegistradas = asistencias.values.sum()

            // 4. VALIDACIÓN DE CONSISTENCIA DE GOLES
            if (totalGolesRegistrados != golesEquipoInput) {
                Toast.makeText(
                    this,
                    "Error: Registraste $totalGolesRegistrados goles en goleadores pero ingresaste $golesEquipoInput goles en el marcador",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // 5. VALIDACIÓN DE CONSISTENCIA DE ASISTENCIAS
            if (totalAsistenciasRegistradas > totalGolesRegistrados) {
                Toast.makeText(
                    this,
                    "Error: No puede haber más asistencias ($totalAsistenciasRegistradas) que goles ($totalGolesRegistrados)",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // 6. VALIDACIÓN: JUGADORES NO EN ALINEACIÓN NO PUEDEN TENER GOLES
            val jugadoresConGoles = goleadores.filter { it.value > 0 }.keys
            val jugadoresConGolesNoAlineados = jugadoresConGoles - alineacion.toSet()

            if (jugadoresConGolesNoAlineados.isNotEmpty()) {
                Toast.makeText(
                    this,
                    "Error: Los jugadores con goles tienen que estar en la alineación",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // 7. VALIDACIÓN: JUGADORES NO EN ALINEACIÓN NO PUEDEN TENER ASISTENCIAS
            val jugadoresConAsistencias = asistencias.filter { it.value > 0 }.keys
            val jugadoresConAsistenciasNoAlineados = jugadoresConAsistencias - alineacion.toSet()

            if (jugadoresConAsistenciasNoAlineados.isNotEmpty()) {
                Toast.makeText(
                    this,
                    "Error: Los jugadores con asistencias tienen que estar en la alineación",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // 8. VALIDACIÓN: MVP DEBE ESTAR EN ALINEACIÓN
            if (mvp != null && !alineacion.contains(mvp)) {
                Toast.makeText(
                    this,
                    "Error: El MVP debe estar en la alineación",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Si pasa todas las validaciones, proceder
            onAlineacionSelected(alineacion)
            onGoleadoresSelected(goleadores)
            onAsistenciasSelected(asistencias)
            onMvpSelected(mvp)
            dialog.dismiss()
        }

        binding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.setContentView(binding.root)
        dialog.show()
    }

    private fun showAddPartidoDialog() {
        val dialog = BottomSheetDialog(this)
        val binding = DialogAddPartidoBinding.inflate(layoutInflater)
        val equipoId = intent.getStringExtra("equipo_id") ?: return
        val competicionMap = mutableMapOf<String, String>()

        var alineacionSeleccionada = mutableListOf<String>()
        var goleadoresMap = mutableMapOf<String, Int>()
        var asistenciasMap = mutableMapOf<String, Int>()
        var jugadorDelPartido: String? = null

        binding.spinnerCompeticion.setOnClickListener {
            binding.spinnerCompeticion.showDropDown()
        }

        // Obtener competiciones de Firestore
        viewModel.competiciones.observe(this) { competiciones ->
            competiciones?.let {
                competicionMap.clear()
                it.forEach { comp -> competicionMap[comp.nombre] = comp.id }

                val competicionAdapter = ArrayAdapter(
                    this,
                    R.layout.dropdown_item,
                    it.map { comp -> comp.nombre }
                )
                binding.spinnerCompeticion.setAdapter(competicionAdapter)
            }
        }

        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        binding.btnAddJugadores.setOnClickListener {
            // Obtener goles del equipo primero
            val golesEquipoInput = try {
                binding.etGolesEquipo.text.toString().toInt()
            } catch (e: NumberFormatException) {
                0
            }

            showJugadoresPartidoDialog(
                equipoId = equipoId,
                golesEquipoInput = golesEquipoInput, // <- Nuevo parámetro
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

        binding.btnSave.setOnClickListener {
            var isValid = true

            // Validar rival
            if (binding.etRival.text.isNullOrBlank()) {
                binding.tilRival.error = getString(R.string.error_campo_obligatorio)
                isValid = false
            } else {
                binding.tilRival.error = null
            }

            // Validar goles equipo
            val golesEquipo = binding.etGolesEquipo.text.toString().toIntOrNull()
            if (golesEquipo == null || golesEquipo < 0) {
                binding.tilGolesEquipo.error = "Valor inválido"
                isValid = false
            } else {
                binding.tilGolesEquipo.error = null
            }

            // Validar goles rival
            val golesRival = binding.etGolesRival.text.toString().toIntOrNull()
            if (golesRival == null || golesRival < 0) {
                binding.tilGolesRival.error = "Valor inválido"
                isValid = false
            } else {
                binding.tilGolesRival.error = null
            }

            if (binding.etTemporada.text.isNullOrBlank()) {
                binding.tilTemporada.error = "La temporada es obligatoria"
                isValid = false
            } else {
                binding.tilTemporada.error = null
            }

            if (binding.spinnerCompeticion.text.isNullOrBlank()) {
                binding.tilCompeticion.error = "Selecciona una competición"
                isValid = false
            } else {
                binding.tilCompeticion.error = null
            }

            if (!isValid) {
                return@setOnClickListener
            }

            try {
                // Obtener el ID de la competición seleccionada
                val competicionNombre = binding.spinnerCompeticion.text.toString()
                val competicionId = competicionMap[competicionNombre] ?: run {
                    Toast.makeText(this, "Error: Competición no válida", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val goleadoresIds = goleadoresMap.flatMap { (jugadorId, cantidad) ->
                    List(cantidad) { jugadorId }
                }

                // Convertir el mapa de asistencias a una lista de IDs
                val asistentesIds = asistenciasMap.flatMap { (jugadorId, cantidad) ->
                    List(cantidad) { jugadorId }
                }

                // VALIDACIÓN FINAL DE CONSISTENCIA (por si el usuario modificó los goles después de cerrar el diálogo)
                val totalGoles = goleadoresMap.values.sum()
                val totalAsistencias = asistenciasMap.values.sum()

                if (totalGoles != (binding.etGolesEquipo.text.toString().toIntOrNull() ?: 0)) {
                    Toast.makeText(
                        this,
                        "La cantidad de goles no coincide con los goleadores registrados",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                if (totalAsistencias > totalGoles) {
                    Toast.makeText(
                        this,
                        "No puede haber más asistencias que goles",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                if (alineacionSeleccionada.size < 11) {
                    Toast.makeText(
                        this,
                        "Debes seleccionar al menos 11 jugadores en la alineación",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                val nuevoPartido = Partido(
                    equipoId = equipoId,
                    fecha = Date(),
                    rival = binding.etRival.text.toString(),
                    golesEquipo = totalGoles,
                    golesRival = binding.etGolesRival.text.toString().toIntOrNull() ?: 0,
                    competicionId = competicionId,
                    competicionNombre = competicionNombre,
                    temporada = binding.etTemporada.text.toString(),
                    fase = binding.etFase.text.toString().takeIf { it.isNotBlank() },
                    jornada = binding.etJornada.text.toString().toIntOrNull(),
                    esLocal = binding.switchLocal.isChecked,
                    alineacionIds = alineacionSeleccionada,
                    goleadoresIds = goleadoresIds,
                    asistentesIds = asistentesIds,
                    jugadorDelPartido = jugadorDelPartido
                )

                viewModel.addPartido(nuevoPartido)
                dialog.dismiss()
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }

        // Limpiar errores al enfocar
        listOf(
            binding.etRival,
            binding.etGolesEquipo,
            binding.etGolesRival,
            binding.etTemporada,
            binding.spinnerCompeticion
        ).forEach { view ->
            view.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    when (view) {
                        binding.etRival -> binding.tilRival.error = null
                        binding.etGolesEquipo -> binding.tilGolesEquipo.error = null
                        binding.etGolesRival -> binding.tilGolesRival.error = null
                        binding.etTemporada -> binding.tilTemporada.error = null
                        binding.spinnerCompeticion -> binding.tilCompeticion.error = null
                    }
                }
            }
        }

        dialog.show()
    }

    private fun showAddJugadorDialog() {
        val dialog = BottomSheetDialog(this)
        val binding = DialogAddJugadorBinding.inflate(layoutInflater)
        val equipoId = intent.getStringExtra("equipo_id") ?: return

        // Configurar el desplegable
        binding.spinnerPosicion.setOnClickListener {
            binding.spinnerPosicion.showDropDown()
        }

        val posiciones = Posicion.entries.map { it.name }
        val posicionAdapter = ArrayAdapter(this, R.layout.dropdown_item, posiciones)
        binding.spinnerPosicion.setAdapter(posicionAdapter)

        binding.btnSave.setOnClickListener {
            var isValid = true

            // Validar nombre
            if (binding.etNombre.text.isNullOrBlank()) {
                binding.tilNombre.error = getString(R.string.error_campo_obligatorio)
                isValid = false
            } else {
                binding.tilNombre.error = null
            }

            // Validar posición
            if (binding.spinnerPosicion.text.isNullOrBlank()) {
                binding.tilPosicion.error = "Selecciona una posición"
                isValid = false
            } else {
                binding.tilPosicion.error = null
            }

            if (!isValid) return@setOnClickListener

            try {
                val nuevoJugador = Jugador(
                    nombre = binding.etNombre.text.toString(),
                    posicion = Posicion.valueOf(binding.spinnerPosicion.text.toString()),
                    equipoId = equipoId
                )
                viewModel.addJugador(nuevoJugador)
                dialog.dismiss()
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }

        // Limpiar errores al enfocar
        listOf(binding.etNombre, binding.spinnerPosicion).forEach { view ->
            view.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    when (view) {
                        binding.etNombre -> binding.tilNombre.error = null
                        binding.spinnerPosicion -> binding.tilPosicion.error = null
                    }
                }
            }
        }

        dialog.setContentView(binding.root)
        dialog.show()
    }

    private fun setupViewModel() {
        val firestore = FirebaseFirestore.getInstance()
        val equipoRepository = EquipoRepositoryImpl(firestore, StatsCalculator)
        val jugadorRepository = JugadorRepositoryImpl(firestore)
        val partidoRepository = PartidoRepositoryImpl(db = firestore, jugadorRepository = jugadorRepository)
        val competicionRepository = CompeticionRepositoryImpl(firestore)

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return EquipoDetailViewModel(
                    repository = equipoRepository,
                    jugadorRepository = jugadorRepository,
                    partidoRepository = partidoRepository,
                    competicionRepository = competicionRepository
                ) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[EquipoDetailViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.equipo.observe(this) { equipo ->
            equipo?.let {
                binding.tvTeamName.text = it.nombre
                cargarImagenBase64(it.imagenBase64)
            }
        }

        viewModel.estadisticas.observe(this) { stats ->
            stats?.let {
                binding.tvMatchesPlayed.text = stats.partidosJugados.toString()
                binding.tvWins.text = stats.victorias.toString()
                binding.tvDraws.text = stats.empates.toString()
                binding.tvLosses.text = stats.derrotas.toString()
            }
        }
    }

    private fun cargarImagenBase64(base64String: String) {
        if (base64String.isEmpty()) {
            binding.ivTeamBadge.setImageResource(R.drawable.ic_default_team_placeholder)
            return
        }

        try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            binding.ivTeamBadge.setImageBitmap(bitmap)
        } catch (e: Exception) {
            binding.ivTeamBadge.setImageResource(R.drawable.ic_default_team_placeholder)
        }
    }
}