package com.example.futboldata

import android.app.Application
import android.content.Context
import com.example.futboldata.utils.FirebaseUtils

class FutbolDataApp : Application() {
    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        FirebaseUtils.initialize(appContext)
    }
}