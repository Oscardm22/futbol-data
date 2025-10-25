package com.example.futboldata.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val sharedPref: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveUser(uid: String, email: String) {
        sharedPref.edit().apply {
            putString("user_uid", uid)
            putString("user_email", email)
            putLong("login_time", System.currentTimeMillis())
            apply()
        }
    }

    fun getCurrentUserUid(): String? = sharedPref.getString("user_uid", null)

    fun clearUser() {
        sharedPref.edit { clear() }
    }
}