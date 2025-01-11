package com.example.tunasid

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tunasid.karyawan.homekaryawan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)

        val emailEditText = findViewById<EditText>(R.id.emailnya)
        val passwordEditText = findViewById<EditText>(R.id.passET)
        val jabatanSpinner = findViewById<Spinner>(R.id.spinner_jabatan)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val rememberMeCheckbox = findViewById<CheckBox>(R.id.rememberMeCheckbox)
        val button1 = findViewById<Button>(R.id.button7)
        val regis = findViewById<TextView>(R.id.button7)
        // Load saved login credentials
        val rememberedEmail = sharedPreferences.getString("email", "")
        val rememberedPassword = sharedPreferences.getString("password", "")
        button1.setOnClickListener {
            val intent1 = Intent(this, register::class.java)
            startActivity(intent1)
        }

        if (!rememberedEmail.isNullOrEmpty() && !rememberedPassword.isNullOrEmpty()) {
            emailEditText.setText(rememberedEmail)
            passwordEditText.setText(rememberedPassword)
            rememberMeCheckbox.isChecked = true
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val jabatan = jabatanSpinner.selectedItem.toString()
            val nama = findViewById<EditText>(R.id.nama).text.toString().trim()

            if (jabatan == "Pilih Jabatan") {
                Toast.makeText(this, "Silakan pilih posisi pekerjaan yang valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(nama, email, password, jabatan, rememberMeCheckbox.isChecked)
        }
    }

    private fun loginUser(
        nama: String?,
        email: String,
        password: String,
        selectedJabatan: String,
        rememberMe: Boolean
    ) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

     
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Memuat data...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user == null) {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Autentikasi gagal, coba lagi", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    val userId = user.uid
                    db.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener { document ->
                            progressDialog.dismiss()
                            val storedJabatan = document?.getString("jabatan")
                            val storedNama = document?.getString("nama")

                            if (storedJabatan != null) {
                                if (storedJabatan == selectedJabatan) {
                                    if (selectedJabatan == "Karyawan" && storedNama?.equals(nama, ignoreCase = true) == true) {
                                        proceedWithLogin(email, password, rememberMe, storedJabatan, storedNama)
                                    } else if (selectedJabatan == "Manajemen") {
                                        proceedWithLogin(email, password, rememberMe, storedJabatan, storedNama)
                                    } else {
                                        Toast.makeText(this, "Nama tidak cocok dengan data terdaftar", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(this, "Posisi pekerjaan tidak sesuai dengan data", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this, "Data jabatan tidak ditemukan", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            progressDialog.dismiss()
                            Toast.makeText(this, "Gagal mendapatkan data: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Autentikasi gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun proceedWithLogin(
        email: String,
        password: String,
        rememberMe: Boolean,
        jabatan: String,
        nama: String?
    ) {
        if (rememberMe) {
            val editor = sharedPreferences.edit()
            editor.putString("email", email)
            editor.putString("password", password)
            editor.apply()
        } else {
            sharedPreferences.edit().clear().apply()
        }

        when (jabatan) {
            "Manajemen" -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            "Karyawan" -> {
                val intent = Intent(this, homekaryawan::class.java)
                intent.putExtra("nama", nama)
                intent.putExtra("jabatan", jabatan)
                intent.putExtra("email", email)
                startActivity(intent)
                finish()
            }
            else -> {
                Toast.makeText(this, "Posisi pekerjaan tidak valid", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
