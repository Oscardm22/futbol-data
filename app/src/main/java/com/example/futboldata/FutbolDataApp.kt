package com.example.futboldata

import android.app.Application
import com.example.futboldata.data.repository.impl.AuthRepositoryImpl
import com.example.futboldata.data.repository.impl.EquipoRepositoryImpl
import com.example.futboldata.data.repository.impl.JugadorRepositoryImpl
import com.example.futboldata.data.repository.impl.PartidoRepositoryImpl
import com.example.futboldata.utils.StatsCalculator
import com.example.futboldata.viewmodel.SharedViewModelFactory
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class FutbolDataApp : Application() {

    lateinit var viewModelFactory: SharedViewModelFactory
        private set

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        setupDependencies()
    }

    private fun setupDependencies() {
        val firestore = FirebaseFirestore.getInstance()
        val firebaseAuth = FirebaseAuth.getInstance()
        val statsCalculator = StatsCalculator() // Instancia creada

        // Pasa statsCalculator al repositorio
        val equipoRepository = EquipoRepositoryImpl(
            db = firestore,
            statsCalculator = statsCalculator // Parámetro añadido
        )

        val authRepository = AuthRepositoryImpl(firebaseAuth)
        val partidoRepository = PartidoRepositoryImpl(firestore)
        val jugadorRepository = JugadorRepositoryImpl(firestore)

        viewModelFactory = SharedViewModelFactory(
            equipoRepository = equipoRepository,
            authRepository = authRepository,
            partidoRepository = partidoRepository,
            jugadorRepository = jugadorRepository
        )
    }
}