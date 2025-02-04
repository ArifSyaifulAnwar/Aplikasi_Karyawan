package com.example.tunasid

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class notifikasi : AppCompatActivity() {

    private lateinit var dataContainer: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifikasi)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buttonBack = findViewById<ImageView>(R.id.imageView4)
        buttonBack.setOnClickListener {
            val intent1 = Intent(this, MainActivity::class.java)
            startActivity(intent1)
        }

        dataContainer = findViewById(R.id.dataContainer)
        loadTodayLateData()

        // set Button untuk menghapus notifikasi dan data yang ada di firebase. *hati hati saati menekan tombol*
//        val btnDelete = findViewById<Button>(R.id.btnDeleteNotif)
//        btnDelete.setOnClickListener {
//            deleteTodayLateData()
//            dataContainer.removeAllViews() // Kosongkan tampilan
//        }

    }

    private fun loadTodayLateData() {
        val db = FirebaseFirestore.getInstance()

        // Ambil tanggal hari ini dengan awal dan akhir hari
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfDay = Timestamp(calendar.time)

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = Timestamp(calendar.time)

        db.collection("absensi")
            .whereEqualTo("keterlambatan", "Terlambat")
            .whereGreaterThanOrEqualTo("timestamp", startOfDay)
            .whereLessThanOrEqualTo("timestamp", endOfDay)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val emptyView = TextView(this).apply {
                        text = "Tidak ada keterlambatan hari ini."
                        textSize = 16f
                        setTextColor(resources.getColor(R.color.black, null))
                        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    }
                    dataContainer.addView(emptyView)
                } else {
                    for (document in documents) {
                        val name = document.getString("name") ?: "Unknown"
                        val timestamp = document.getTimestamp("timestamp")?.toDate() ?: Date()
                        val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp)
                        createNotificationCard(name, formattedTime)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createNotificationCard(name: String, time: String) {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.notipterlambat, dataContainer, false)

        val namaKaryawanTextView = cardView.findViewById<TextView>(R.id.namaKaryawan)
        val terlambatTextView = cardView.findViewById<TextView>(R.id.terlambat)


        namaKaryawanTextView.text = name
        terlambatTextView.text = "Terlambat: $time"

        dataContainer.addView(cardView)
    }


    private fun deleteTodayLateData() {
        val db = FirebaseFirestore.getInstance()

        // Ambil tanggal hari ini dengan awal dan akhir hari
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfDay = Timestamp(calendar.time)

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = Timestamp(calendar.time)

        db.collection("absensi")
            .whereEqualTo("keterlambatan", "Terlambat")
            .whereGreaterThanOrEqualTo("timestamp", startOfDay)
            .whereLessThanOrEqualTo("timestamp", endOfDay)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("absensi").document(document.id).delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Notifikasi berhasil dihapus.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal menghapus notifikasi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}
