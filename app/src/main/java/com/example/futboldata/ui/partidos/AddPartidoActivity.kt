package com.example.futboldata.ui.partidos

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.adapter.JugadoresPartidoAdapter
import com.example.futboldata.data.model.*
import com.example.futboldata.databinding.ActivityAddPartidoBinding
import com.example.futboldata.viewmodel.PartidoViewModel
import java.util.*

class AddPartidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPartidoBinding
    private val viewModel: PartidoViewModel by viewModels()
    private lateinit var jugadoresAdapter: JugadoresPartidoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPartidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val equipoId = intent.getStringExtra("EQUIPO_ID") ?: ""

        // Configurar RecyclerView
        jugadoresAdapter = JugadoresPartidoAdapter { jugador, participacion ->
            viewModel.actualizarParticipacion(jugador.id, participacion)
        }

        binding.rvJugadores.apply {
            layoutManager = LinearLayoutManager(this@AddPartidoActivity)
            adapter = jugadoresAdapter
        }

        // Cargar jugadores del equipo
        viewModel.cargarJugadores(equipoId)
        viewModel.jugadores.observe(this) { jugadores ->
            jugadoresAdapter.submitList(jugadores)
        }

        binding.btnGuardar.setOnClickListener {
            guardarPartido(equipoId)
        }
    }

    private fun guardarPartido(equipoId: String) {
        val golesFavor = binding.etGolesFavor.text.toString().toIntOrNull() ?: 0
        val golesContra = binding.etGolesContra.text.toString().toIntOrNull() ?: 0

        val partido = Partido(
            equipoId = equipoId,
            fecha = Date(),
            rival = binding.etRival.text.toString(),
            resultado = "$golesFavor-$golesContra",
            competicionId = "comp_default", // Actualiza esto según tu UI
            competicionNombre = binding.etCompeticion.text.toString(),
            temporada = "2023-24", // Actualiza según necesidad
            fase = "Fase de grupos", // Actualiza según necesidad
            jornada = binding.etJornada.text.toString().toIntOrNull(),
            alineacion = viewModel.obtenerAlineacionActual(),
            goleadores = viewModel.obtenerGoles(),
            asistentes = viewModel.obtenerAsistencias()
        )

        viewModel.addPartido(partido)
        finish()
    }
}