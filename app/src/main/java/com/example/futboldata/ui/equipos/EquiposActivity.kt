package com.example.futboldata.ui.equipos

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.futboldata.R
import com.example.futboldata.adapter.EquiposAdapter
import com.example.futboldata.databinding.ActivityEquiposBinding
import com.example.futboldata.ui.auth.LoginActivity
import com.example.futboldata.viewmodel.EquipoViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.FutbolDataApp
import com.example.futboldata.data.model.Equipo
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.Date

class EquiposActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEquiposBinding
    private lateinit var auth: FirebaseAuth
    private val viewModel: EquipoViewModel by viewModels {
        (application as FutbolDataApp).viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEquiposBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        auth = Firebase.auth

        setupRecyclerView()
        setupObservers()
        setupFAB()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun setupRecyclerView() {
        binding.rvEquipos.apply {
            layoutManager = LinearLayoutManager(this@EquiposActivity)
            adapter = EquiposAdapter(emptyList(),
                { equipoId -> abrirDetalleEquipo(equipoId) },
                { equipoId -> mostrarDialogoEliminacion(equipoId) }
            )
        }
    }

    private fun setupObservers() {
        viewModel.equiposState.observe(this) { state ->
            when (state) {
                is EquipoViewModel.EquipoState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is EquipoViewModel.EquipoState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    // Convertimos el Map a List<Pair<String, Equipo>>
                    val equiposList = state.equipos.entries.map { it.toPair() }
                    (binding.rvEquipos.adapter as EquiposAdapter).updateList(equiposList)
                }
                is EquipoViewModel.EquipoState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.mensaje, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.operacionState.observe(this) { state ->
            when (state) {
                is EquipoViewModel.OperacionState.Loading -> {
                    // Mostrar progreso en operaciones
                }
                is EquipoViewModel.OperacionState.Success -> {
                    Toast.makeText(this, state.mensaje, Toast.LENGTH_SHORT).show()
                }
                is EquipoViewModel.OperacionState.Error -> {
                    Toast.makeText(this, state.mensaje, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupFAB() {
        binding.fabAddEquipo.setOnClickListener {
            abrirDialogoCreacionEquipo()
        }
    }

    private fun abrirDialogoCreacionEquipo() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nuevo_equipo, null)
        val editTextNombre = dialogView.findViewById<TextInputEditText>(R.id.etNombreEquipo) // ID corregido

        AlertDialog.Builder(this)
            .setTitle("Nuevo Equipo")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = editTextNombre.text.toString().trim()
                if (nombre.isNotEmpty()) {
                    val nuevoEquipo = Equipo(
                        nombre = nombre,
                        fechaCreacion = Date()
                    )
                    viewModel.guardarEquipo(nuevoEquipo)
                } else {
                    Toast.makeText(this, "Ingresa un nombre válido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }

    private fun abrirDetalleEquipo(equipoId: String) {
        val intent = Intent(this, EquipoDetailActivity::class.java).apply {
            putExtra("equipo_id", equipoId)
        }
        startActivity(intent)
    }

    private fun mostrarDialogoEliminacion(equipoId: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar equipo")
            .setMessage("¿Seguro que quieres eliminar este equipo?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarEquipo(equipoId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}