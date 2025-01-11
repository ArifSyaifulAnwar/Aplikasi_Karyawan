package com.example.tunasid


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore

class CheckDataWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val db = FirebaseFirestore.getInstance()

        // Ambil data terakhir dari SharedPreferences
        val sharedPreferences = applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val lastNotifiedId = sharedPreferences.getString("last_notified_id", null)

        db.collection("absensi")
            .whereEqualTo("keterlambatan", "Terlambat")
            .get()
            .addOnSuccessListener { documents ->
                val names = mutableListOf<String>()
                var lastDocumentId: String? = null

                for (document in documents) {
                    val documentId = document.id
                    val name = document.getString("name")

                    // Tambahkan nama ke daftar jika ada data baru
                    if (documentId != lastNotifiedId && name != null) {
                        names.add(name)
                        lastDocumentId = documentId
                    }
                }

                if (names.isNotEmpty()) {
                    // Tampilkan notifikasi dengan daftar nama
                    val namesString = names.joinToString(", ")
                    showNotification(
                        "Data Terlambat",
                        "Karyawan terlambat: $namesString."
                    )

                    // Simpan ID dokumen terakhir yang diberi notifikasi
                    if (lastDocumentId != null) {
                        sharedPreferences.edit()
                            .putString("last_notified_id", lastDocumentId)
                            .apply()
                    }
                }
            }
            .addOnFailureListener {
                // Gagal memeriksa data
            }

        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "data_delay_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notifikasi Data Terlambat",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.notifikasi) // Ganti dengan ikon Anda
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(1, notification)
    }
}

