package com.example.futboldata.ui.equipos

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.futboldata.R
import com.example.futboldata.adapter.EquiposAdapter
import com.example.futboldata.databinding.ActivityEquiposBinding
import com.example.futboldata.ui.auth.LoginActivity
import com.example.futboldata.viewmodel.EquipoViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.FutbolDataApp
import com.example.futboldata.data.model.Equipo
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
        val textInputLayout = dialogView.findViewById<TextInputLayout>(R.id.tilNombreEquipo)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.etNombreEquipo)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Nuevo Equipo")
            .setView(dialogView)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        // Configura el color de los botones y título
        dialog.setOnShowListener {
            val textView = dialog.findViewById<TextView>(android.R.id.title)
            textView?.setTextColor(ContextCompat.getColor(this, R.color.Fondo))
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, R.color.Fondo))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(this, R.color.Fondo))

            // Manejo manual del botón positivo
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                val nombre = editText.text.toString().trim()
                val (esValido, mensajeError) = validarNombreEquipo(nombre)

                if (esValido) {
                    val nuevoEquipo = Equipo(
                        nombre = nombre,
                        fechaCreacion = Date()
                    )
                    viewModel.guardarEquipo(nuevoEquipo)
                    dialog.dismiss() // Solo cerramos si es válido
                } else {
                    textInputLayout.error = mensajeError
                    // Mantenemos el diálogo abierto para que corrija el error
                }
            }
        }

        // Limpiar error cuando el usuario empieza a editar
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                textInputLayout.error = null
            }
        }

        dialog.show()
    }

    private fun validarNombreEquipo(nombre: String): Pair<Boolean, String> {
        return when {
            nombre.isEmpty() -> false to "El nombre no puede estar vacío"
            nombre.length < 3 -> false to "El nombre debe tener al menos 3 caracteres"
            nombre.length > 30 -> false to "El nombre no puede exceder 30 caracteres"
            else -> true to ""
        }
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