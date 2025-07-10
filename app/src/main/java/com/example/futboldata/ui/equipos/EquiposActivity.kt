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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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
            adapter = EquiposAdapter(emptyList(), { equipo ->
                // Click en equipo
                abrirDetalleEquipo(equipo.id)
            }, { equipo ->
                // Click en eliminar
                mostrarDialogoEliminacion(equipo)
            })
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
                    (binding.rvEquipos.adapter as EquiposAdapter).updateList(state.equipos)
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
        val dialog = AlertDialog.Builder(this)
            .setTitle("Nuevo Equipo")
            .setView(R.layout.dialog_nuevo_equipo)
            .setPositiveButton("Guardar") { _, _ ->
                // Lógica para guardar
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    private fun abrirDetalleEquipo(equipoId: String) {
        val intent = Intent(this, EquipoDetailActivity::class.java).apply {
            putExtra("equipo_id", equipoId)
        }
        startActivity(intent)
    }

    private fun mostrarDialogoEliminacion(equipo: Equipo) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar equipo")
            .setMessage("¿Seguro que quieres eliminar a ${equipo.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarEquipo(equipo.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}