package com.example.tunasid.karyawan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.tunasid.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Profile : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var email: String = "Email tidak ditemukan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (userId.isNotEmpty()) {
            fetchProfileImage(userId)
        } else {
            Toast.makeText(this, "Gagal mendapatkan user ID. Harap login ulang.", Toast.LENGTH_SHORT).show()
        }

        // Ambil data yang diterima dari intent
        val nama = intent.getStringExtra("nama") ?: "Nama tidak tersedia"
        val jabatan = intent.getStringExtra("jabatan") ?: "Jabatan tidak tersedia"
        val emailFromIntent = intent.getStringExtra("email")

        // Tampilkan nama dan jabatan
        val namaTextView = findViewById<TextView>(R.id.namaKaryawan)
        val jabatanTextView = findViewById<TextView>(R.id.jabatan)
        val buttonEdit = findViewById<Button>(R.id.buttonEdit)

        namaTextView.text = nama
        jabatanTextView.text = jabatan

        // Jika email tidak ada di intent, ambil dari Firestore berdasarkan nama
        if (!emailFromIntent.isNullOrBlank()) {
            email = emailFromIntent
            findViewById<TextView>(R.id.email).text = email
        } else {
            fetchEmailByName(nama) { fetchedEmail ->
                email = fetchedEmail // Simpan email yang diambil
                findViewById<TextView>(R.id.email).text = email
            }
        }

        // Set listener untuk button edit
        buttonEdit.setOnClickListener {
            val intent = Intent(this, editProfil::class.java)
            intent.putExtra("nama", nama)
            intent.putExtra("email", email) // Pass email to editProfil
            startActivity(intent)
        }
    }

    private fun fetchEmailByName(nama: String, callback: (String) -> Unit) {
        val emailTextView = findViewById<TextView>(R.id.email)

        db.collection("users")
            .whereEqualTo("nama", nama)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val email = documents.documents[0].getString("email") ?: "Email tidak ditemukan"
                    emailTextView.text = email
                    callback(email) // Return the fetched email
                } else {
                    val notFoundMessage = "Email tidak ditemukan"
                    emailTextView.text = notFoundMessage
                    callback(notFoundMessage)
                }
            }
            .addOnFailureListener { e ->
                val errorMessage = "Gagal memuat email"
                emailTextView.text = errorMessage
                callback(errorMessage)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun fetchProfileImage(userId: String) {
        val imageView = findViewById<ShapeableImageView>(R.id.fotoprofil)

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profileImageUrl = document.getString("profileImage")
                    if (!profileImageUrl.isNullOrEmpty()) {
                        // Gunakan Glide untuk memuat gambar
                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.circle_background) // Placeholder saat gambar dimuat
                            .error(R.drawable.circle_background) // Gambar default jika URL gagal dimuat
                            .into(imageView)
                    } else {
                        Toast.makeText(this, "Foto profil tidak ditemukan.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Pengguna tidak ditemukan di Firestore.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat foto profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}
