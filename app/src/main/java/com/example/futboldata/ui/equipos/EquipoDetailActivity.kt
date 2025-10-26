package com.example.futboldata.ui.equipos

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.futboldata.databinding.ActivityEquipoDetailBinding
import com.example.futboldata.data.repository.impl.EquipoRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import com.example.futboldata.utils.StatsCalculator
import com.example.futboldata.R
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
import com.example.futboldata.data.repository.impl.CompeticionRepositoryImpl
import com.example.futboldata.data.repository.impl.JugadorRepositoryImpl
import com.example.futboldata.data.repository.impl.PartidoRepositoryImpl
import com.example.futboldata.databinding.DialogSeleccionarCompeticionBinding
import com.example.futboldata.ui.equipos.dialogs.AddJugadorDialog
import com.example.futboldata.ui.equipos.dialogs.AddPartidoDialog
import com.example.futboldata.ui.equipos.fragments.DestacadosFragment
import com.example.futboldata.utils.ImageLoader

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

    private fun showAddPartidoDialog() {
        val equipoId = intent.getStringExtra("equipo_id") ?: return

        val dialog = AddPartidoDialog.newInstance(equipoId)
        dialog.setOnPartidoAddedListener { partido ->
            viewModel.addPartido(partido)
        }
        dialog.show(supportFragmentManager, "AddPartidoDialog")
    }

    private fun showAddJugadorDialog() {
        val equipoId = intent.getStringExtra("equipo_id") ?: return

        val dialog = AddJugadorDialog.newInstance(equipoId)
        dialog.setOnJugadorAddedListener { jugador ->
            viewModel.addJugador(jugador)
        }
        dialog.show(supportFragmentManager, "AddJugadorDialog")
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
        ImageLoader.loadBase64Image(
            base64 = base64String,
            imageView = binding.ivTeamBadge,
            defaultDrawable = R.drawable.ic_default_team_placeholder,
            targetSize = 150
        )
    }
}