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
import com.bumptech.glide.Glide
import com.example.tunasid.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            fetchProfileImage(userId)
        } else {
            Toast.makeText(this, "Pengguna tidak ditemukan. Harap login ulang.", Toast.LENGTH_SHORT).show()
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
                    // Ubah data ke dalam daftar Pair untuk menyimpan sementara
                    val absensiList = documents.map { document ->
                        val keterlambatan = document.getString("keterlambatan") ?: "Tidak Diketahui"
                        val timestampDate = document.getTimestamp("timestamp")?.toDate()
                        Pair(keterlambatan, timestampDate)
                    }

                    // Urutkan data berdasarkan timestamp (tanggal terbaru di atas)
                    val sortedAbsensiList = absensiList.sortedByDescending { it.second }

                    // Tampilkan data yang telah diurutkan
                    for ((keterlambatan, timestamp) in sortedAbsensiList) {
                        val timestampString = timestamp?.let {
                            SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).format(it)
                        } ?: "Tidak Tersedia"

                        val cardView = createCardView(name, keterlambatan, timestampString, jabatan)
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
        val profileImageView = card.findViewById<ImageView>(R.id.profileImage)

        // Set nama karyawan
        namaTextView.text = name
        jabatanKaryawannya.text = jabatanKaryawan

        fetchProfileImageForCard(name, profileImageView)

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
    private fun fetchProfileImage(userId: String) {
        if (userId.isEmpty()) {
            // Jika userId kosong, tampilkan error dan gunakan placeholder
            val imageView = findViewById<ImageView>(R.id.fotoprofil)
            Glide.with(this)
                .load(R.drawable.box_background)
                .into(imageView)
            Toast.makeText(this, "User ID kosong. Tidak dapat memuat gambar.", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")
        val imageView = findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.fotoprofil)

        storageRef.downloadUrl
            .addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.box_background) // Placeholder kotak
                    .error(R.drawable.box_background) // Placeholder jika gagal
                    .into(imageView)
            }
            .addOnFailureListener { e ->
                Glide.with(this)
                    .load(R.drawable.box_background)
                    .into(imageView)
                Toast.makeText(this, "Belum ada foto profil.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchProfileImageForCard(userId: String, imageView: ImageView) {
        if (userId.isEmpty()) {
            // Jika nama kosong, gunakan gambar default
            Glide.with(this)
                .load(R.drawable.box_background)
                .into(imageView)
            Toast.makeText(this, "Nama kosong. Tidak dapat memuat gambar.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "Belum ada Gambar yang di upload"
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")

        storageRef.downloadUrl
            .addOnSuccessListener { uri ->
                // Jika URL berhasil diambil, gunakan Glide untuk memuat gambar
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.box_background) // Placeholder saat loading
                    .error(R.drawable.box_background) // Placeholder jika gagal memuat gambar
                    .into(imageView)
            }
            .addOnFailureListener { e ->
                // Jika gagal mengambil URL, gunakan gambar default dan tampilkan error
                Glide.with(this)
                    .load(R.drawable.box_background)
                    .into(imageView)
                //Toast.makeText(this, "Gagal memuat gambar untuk $userId: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace() // Debugging error
            }
    }



}
