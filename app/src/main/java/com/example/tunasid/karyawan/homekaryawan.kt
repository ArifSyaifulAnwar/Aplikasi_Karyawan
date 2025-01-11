package com.example.tunasid.karyawan

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tunasid.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class homekaryawan : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_homekaryawan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val namaKaryawan = intent.getStringExtra("nama") ?: "Nama tidak tersedia"
        val jabatanKaryawan = intent.getStringExtra("jabatan") ?: "Jabatan tidak tersedia"
        val emailKaryawan = intent.getStringExtra("email") ?: "Email tidak tersedia"
        val namaTextView = findViewById<TextView>(R.id.namaKaryawan)
        val jabatanTextView = findViewById<TextView>(R.id.jabatan)
        val buttonBack = findViewById<ImageView>(R.id.fotoprofil)

        buttonBack.setOnClickListener {
            val intent1 = Intent(this, Profile::class.java)
            intent1.putExtra("nama", namaKaryawan)
            intent1.putExtra("jabatan", jabatanKaryawan)
            intent1.putExtra("email", emailKaryawan)
            startActivity(intent1)
        }

        namaTextView.text = namaKaryawan
        jabatanTextView.text = jabatanKaryawan

        fetchAbsensiByName(namaKaryawan, jabatanKaryawan)
    }

    private fun fetchAbsensiByName(name: String, jabatan: String) {
        val db = FirebaseFirestore.getInstance()
        val containerLayout = findViewById<LinearLayout>(R.id.containerLayout)

        if (name.isBlank() || jabatan.isBlank()) {
            Toast.makeText(this, "Nama atau jabatan tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("absensi")
            .whereEqualTo("name", name)
            .limit(10) // Batasi hanya 10 dokumen
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val noDataTextView = TextView(this).apply {
                        text = "Belum ada data untuk nama karyawan ini."
                        textSize = 16f
                        setTextColor(ContextCompat.getColor(context, R.color.black))
                        gravity = Gravity.CENTER
                    }
                    containerLayout.addView(noDataTextView)
                } else {
                    for (document in documents) {
                        val keterlambatan = document.getString("keterlambatan") ?: "Tidak Diketahui"
                        val timestampDate = document.getTimestamp("timestamp")?.toDate()
                        val timestamp = timestampDate?.toString() ?: "Tidak Tersedia"
                        val cardView = createCardView(name, keterlambatan, timestamp, jabatan)
                        containerLayout.addView(cardView)
                    }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                val errorTextView = TextView(this).apply {
                    text = "Gagal memuat data. Error: ${e.message}"
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.white))
                    gravity = Gravity.CENTER
                }
                containerLayout.addView(errorTextView)
            }
    }

    private fun createCardView(name: String, keterlambatan: String, timestamp: String, jabatanKaryawan: String): View {
        val inflater = LayoutInflater.from(this)
        val card = inflater.inflate(R.layout.cardperorang, null)

        val namaTextView = card.findViewById<TextView>(R.id.namaKaryawan)
        val statusAbsen = card.findViewById<TextView>(R.id.statusAbsen)
        val tanggalAbsen = card.findViewById<TextView>(R.id.tanggalAbsen)
        val jabatanKaryawannya = card.findViewById<TextView>(R.id.jabatanKaryawan)

        // Set nama karyawan
        namaTextView.text = name
        jabatanKaryawannya.text = jabatanKaryawan

        // Set status absen
        statusAbsen.text = if (keterlambatan == "Terlambat") "Terlambat" else "Tepat Waktu"
        if (keterlambatan == "Terlambat") {
            statusAbsen.setBackgroundResource(R.drawable.button_terlambat)
            statusAbsen.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            statusAbsen.setBackgroundResource(R.drawable.button_tepat_waktu)
            statusAbsen.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        // Set tanggal absen dengan format baris baru
        val formatTanggal = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        val formatJam = SimpleDateFormat("HH:mm", Locale("id", "ID"))
        val timestampDate = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(timestamp)
        if (timestampDate != null) {
            val tanggalFormatted = formatTanggal.format(timestampDate)
            val jamFormatted = formatJam.format(timestampDate)
            tanggalAbsen.text = "$tanggalFormatted\n$jamFormatted"
        } else {
            tanggalAbsen.text = "Timestamp tidak tersedia"
        }

        // Menambahkan margin antar card
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 8, 16, 8) // Margin: kiri, atas, kanan, bawah
        card.layoutParams = layoutParams

        // Tambahkan klik pada card jika diperlukan
        card.setOnClickListener {
            Toast.makeText(this, "Klik pada absensi: $name", Toast.LENGTH_SHORT).show()
        }

        return card
    }
}
