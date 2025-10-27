package com.example.futboldata.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class PermissionManager {

    companion object {
        fun forActivity(activity: AppCompatActivity): PermissionManager {
            return PermissionManager().apply {
                this.activity = activity
                this.fragment = null
                registerPermissionLauncher()
            }
        }

        @Suppress("UNUSED")
        fun forFragment(fragment: Fragment): PermissionManager {
            return PermissionManager().apply {
                this.fragment = fragment
                this.activity = null
                registerPermissionLauncher()
            }
        }
    }

    private var activity: AppCompatActivity? = null
    private var fragment: Fragment? = null
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var onPermissionResult: ((Boolean) -> Unit)? = null

    private val context
        get() = activity ?: fragment?.requireContext()
        ?: throw IllegalStateException("PermissionManager not initialized")

    private fun registerPermissionLauncher() {
        val permissionContract = ActivityResultContracts.RequestPermission()

        if (activity != null) {
            permissionLauncher = activity!!.registerForActivityResult(permissionContract) { isGranted ->
                onPermissionResult?.invoke(isGranted)
            }
        } else if (fragment != null) {
            permissionLauncher = fragment!!.registerForActivityResult(permissionContract) { isGranted ->
                onPermissionResult?.invoke(isGranted)
            }
        }
    }

    fun checkImagePermission(): Boolean {
        val permission = getRequiredPermission()
        return permission?.let {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        } != false
    }

    fun getRequiredPermission(): String? {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                Manifest.permission.READ_MEDIA_IMAGES
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            else -> null
        }
    }

    fun requestImagePermission(onResult: (Boolean) -> Unit) {
        getRequiredPermission()?.let { permission ->
            onPermissionResult = onResult
            permissionLauncher.launch(permission)
        } ?: run {
            onResult(true)
        }
    }

    fun shouldShowRationale(): Boolean {
        val permission = getRequiredPermission() ?: return false
        return when {
            activity != null -> {
                androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission)
            }
            fragment != null -> {
                fragment!!.shouldShowRequestPermissionRationale(permission)
            }
            else -> false
        }
    }

    fun checkAndRequestImagePermission(
        onGranted: () -> Unit,
        onDenied: () -> Unit = {},
        onRationale: (() -> Unit)? = null
    ) {
        if (checkImagePermission()) {
            onGranted()
        } else {
            if (shouldShowRationale() && onRationale != null) {
                onRationale()
            } else {
                requestImagePermission { isGranted ->
                    if (isGranted) {
                        onGranted()
                    } else {
                        onDenied()
                    }
                }
            }
        }
    }
}