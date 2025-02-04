package com.example.tunasid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.app.AlertDialog
import android.content.SharedPreferences
import android.provider.Settings
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

class SplashScreen : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        // Periksa izin notifikasi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission()
        } else {
            proceedToLoginScreen()
        }

    }

    private fun checkNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Izin sudah diberikan
            proceedToLoginScreen()
        } else {
            // Tampilkan dialog untuk meminta izin
            requestNotificationPermission()
        }
    }

    private fun requestNotificationPermission() {
        val launcher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Izin diberikan
                proceedToLoginScreen()
            } else {
                // Izin ditolak
                showPermissionDeniedDialog()
            }
        }
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun showPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Izin Notifikasi Ditolak")
        builder.setMessage("Aplikasi ini membutuhkan izin untuk mengirimkan notifikasi. Anda dapat mengaktifkannya di pengaturan.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            proceedToLoginScreen()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun proceedToLoginScreen() {
        Handler().postDelayed({
            val intent = Intent(this, login::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}