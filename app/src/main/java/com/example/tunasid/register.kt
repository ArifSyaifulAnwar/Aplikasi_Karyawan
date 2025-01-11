package com.example.tunasid

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class register : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailnya)
        val namePerson = findViewById<EditText>(R.id.name)
        val passwordEditText = findViewById<EditText>(R.id.passET)
        val confirmPasswordEditText = findViewById<EditText>(R.id.passET1)
        val spinnerJabatan = findViewById<Spinner>(R.id.spinner_jabatan)
        val button1 = findViewById<Button>(R.id.button6)
        button1.setOnClickListener {
            val intent1 = Intent(this, login::class.java)
            startActivity(intent1)
        }


        val button2 = findViewById<Button>(R.id.btnRegister)
        button2.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            val jabatan = spinnerJabatan.selectedItem.toString()
            val nama = namePerson.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Masukkan email yang valid"
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 1) {
                passwordEditText.error = "Kata sandi harus minimal 1 karakter"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }

            if (nama.isEmpty()) {
                namePerson.error = "Nama tidak boleh kosong"
                namePerson.requestFocus()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                confirmPasswordEditText.error = "Kata sandi tidak cocok"
                confirmPasswordEditText.requestFocus()
                return@setOnClickListener
            }

            if (jabatan == "Pilih Jabatan") {
                Toast.makeText(this, "Silakan pilih jabatan yang valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(nama, email, password, jabatan)
        }

        val signin = findViewById<TextView>(R.id.textView6)
        signin.setOnClickListener {
            val intent1 = Intent(this, login::class.java)
            startActivity(intent1)
        }
    }

    private fun registerUser(nama : String,email: String, password: String, jabatan: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val userId = user?.uid

                val userMap = hashMapOf(
                    "nama" to nama,
                    "email" to email,
                    "password" to password,
                    "jabatan" to jabatan
                )

                if (userId != null) {
                    db.collection("users").document(userId).set(userMap).addOnSuccessListener {
                        Toast.makeText(this, "Pendaftaran berhasil", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, login::class.java)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal menyimpan pengguna: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Pendaftaran gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result?.signInMethods
                if (signInMethods.isNullOrEmpty()) {
                    // Email belum terdaftar, lanjutkan registrasi
                    checkEmailExists(nama, email, password, jabatan)
                } else {
                    // Email sudah terdaftar
                    Toast.makeText(this, "Email sudah terdaftar, silakan gunakan email lain.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Gagal memeriksa email
                Toast.makeText(this, "Terjadi kesalahan saat memvalidasi email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }



    }
    private fun checkEmailExists(nama: String, email: String, password: String, jabatan: String) {
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result?.signInMethods
                if (signInMethods.isNullOrEmpty()) {
                    // Email belum terdaftar, lanjutkan registrasi
                    registerUser(nama, email, password, jabatan)
                } else {
                    // Email sudah terdaftar
                    Toast.makeText(this, "Email sudah terdaftar, silakan gunakan email lain.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Gagal memeriksa email
                Toast.makeText(this, "Terjadi kesalahan saat memvalidasi email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}