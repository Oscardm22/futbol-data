package com.example.futboldata

import android.app.Application
import com.example.futboldata.data.repository.impl.AuthRepositoryImpl
import com.example.futboldata.data.repository.impl.CompeticionRepositoryImpl
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
        val statsCalculator = StatsCalculator

        val jugadorRepository = JugadorRepositoryImpl(firestore)

        val equipoRepository = EquipoRepositoryImpl(
            db = firestore,
            statsCalculator = statsCalculator
        )

        val authRepository = AuthRepositoryImpl(firebaseAuth)
        val partidoRepository = PartidoRepositoryImpl(db = firestore, jugadorRepository = jugadorRepository)
        val competicionRepository = CompeticionRepositoryImpl(firestore)

        viewModelFactory = SharedViewModelFactory(
            equipoRepository = equipoRepository,
            authRepository = authRepository,
            partidoRepository = partidoRepository,
            jugadorRepository = jugadorRepository,
            competicionRepository = competicionRepository
        )
    }
}