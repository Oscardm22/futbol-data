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
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.futboldata.data.model.Posicion
import com.example.futboldata.data.model.TipoCompeticion
import com.example.futboldata.data.repository.impl.JugadorRepositoryImpl
import com.example.futboldata.data.repository.impl.PartidoRepositoryImpl
import com.example.futboldata.databinding.DialogAddJugadorBinding
import com.example.futboldata.databinding.DialogAddPartidoBinding
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

        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getTitle(position)
        }.attach()
    }

    inner class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        private val fragments = mutableListOf<Pair<Fragment, String>>()

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

    private fun showAddPartidoDialog() {
        val dialog = BottomSheetDialog(this)
        val binding = DialogAddPartidoBinding.inflate(layoutInflater)
        val equipoId = intent.getStringExtra("equipo_id") ?: return

        // Configurar spinner de competición
        val competiciones = TipoCompeticion.entries.map { it.name }
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, competiciones)
        binding.spinnerCompeticion.setAdapter(adapter)

        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        binding.btnSave.setOnClickListener {
            // Validar campos obligatorios
            if (binding.etRival.text.isNullOrBlank()) {
                binding.etRival.error = getString(R.string.error_campo_obligatorio)
                return@setOnClickListener
            }

            // Validar formato del resultado
            val resultado = binding.etResultado.text.toString()
            if (!resultado.matches(Regex("\\d+-\\d+"))) {
                binding.etResultado.error = getString(R.string.error_formato_resultado)
                return@setOnClickListener
            }

            try {
                val nuevoPartido = Partido(
                    equipoId = equipoId,
                    fecha = Date(),
                    rival = binding.etRival.text.toString(),
                    resultado = resultado,
                    competicionNombre = binding.spinnerCompeticion.text.toString(),
                    temporada = binding.etTemporada.text.toString(),
                    fase = binding.etFase.text.toString().takeIf { it.isNotBlank() },
                    jornada = binding.etJornada.text.toString().toIntOrNull(),
                    esLocal = binding.switchLocal.isChecked
                )

                viewModel.addPartido(nuevoPartido)
                dialog.dismiss()
            } catch (e: IllegalArgumentException) {
                Toast.makeText(this, R.string.error_competicion_invalida, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, R.string.error_generico, Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showAddJugadorDialog() {
        val dialog = BottomSheetDialog(this)
        val binding = DialogAddJugadorBinding.inflate(layoutInflater)
        val equipoId = intent.getStringExtra("equipo_id") ?: return

        // Configurar spinner de posición
        val posiciones = Posicion.entries.map { it.name }
        val posicionAdapter = ArrayAdapter(this, R.layout.dropdown_item, posiciones)
        binding.spinnerPosicion.setAdapter(posicionAdapter)

        binding.btnSave.setOnClickListener {
            val nuevoJugador = Jugador(
                nombre = binding.etNombre.text.toString(),
                posicion = Posicion.valueOf(binding.spinnerPosicion.text.toString()),
                equipoId = equipoId
            )

            viewModel.addJugador(nuevoJugador)
            dialog.dismiss()
        }

        dialog.setContentView(binding.root)
        dialog.show()
    }

    private fun setupViewModel() {
        val firestore = FirebaseFirestore.getInstance()
        val equipoRepository = EquipoRepositoryImpl(firestore, StatsCalculator)
        val jugadorRepository = JugadorRepositoryImpl(firestore)
        val partidoRepository = PartidoRepositoryImpl(firestore)

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return EquipoDetailViewModel(
                    repository = equipoRepository,
                    jugadorRepository = jugadorRepository,
                    partidoRepository = partidoRepository
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