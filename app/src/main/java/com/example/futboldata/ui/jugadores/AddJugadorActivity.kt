package com.example.futboldata.ui.jugadores

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.futboldata.adapter.PosicionSpinnerAdapter
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.model.Posicion
import com.example.futboldata.databinding.ActivityAddJugadorBinding
import com.example.futboldata.data.managers.PosicionManager
import com.google.firebase.firestore.FirebaseFirestore

class AddJugadorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddJugadorBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddJugadorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()
        setupSaveButton()
    }

    private fun setupSpinner() {
        binding.spinnerPosicion.adapter = PosicionSpinnerAdapter(
            this,
            PosicionManager.posiciones
        )
    }

    private fun setupSaveButton() {
        binding.btnGuardar.setOnClickListener {
            guardarJugador()
        }
    }

    private fun guardarJugador() {
        val nombre = binding.etNombre.text.toString().trim()
        val posicion = binding.spinnerPosicion.selectedItem as Posicion
        val equipoId = intent.getStringExtra("EQUIPO_ID") ?: ""

        if (nombre.isEmpty()) {
            binding.etNombre.error = "Ingrese un nombre"
            return
        }

        val jugador = Jugador(
            nombre = nombre,
            equipoId = equipoId,
            posicion = posicion
        )

        db.collection("jugadores")
            .add(jugador)
            .addOnSuccessListener {
                Toast.makeText(this, "Jugador añadido", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}