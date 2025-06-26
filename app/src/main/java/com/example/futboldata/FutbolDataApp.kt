package com.example.futboldata

import android.app.Application
import com.google.firebase.FirebaseApp

class FutbolDataApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}