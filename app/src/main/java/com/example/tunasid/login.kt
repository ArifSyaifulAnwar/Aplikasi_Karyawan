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
import androidx.appcompat.app.AlertDialog
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
        val regis = findViewById<TextView>(R.id.textView6)
        val forgotPasswordTextView = findViewById<TextView>(R.id.lupapw)


        regis.setOnClickListener {
            val intent1 = Intent(this, register::class.java)
            startActivity(intent1)
        }
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

        forgotPasswordTextView.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                emailEditText.error = "Email is required to reset password"
                emailEditText.requestFocus()
                return@setOnClickListener
            }
            sendPasswordResetEmail(email)
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
                                    } else if (selectedJabatan == "Manager") {
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
            "Manager" -> {
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

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newPasswordDialog = AlertDialog.Builder(this)
                    newPasswordDialog.setTitle("Enter New Password")
                    val input = EditText(this)
                    newPasswordDialog.setView(input)

                    newPasswordDialog.setPositiveButton("OK") { dialog, which ->
                        val newPassword = input.text.toString()
                        auth.signInWithEmailAndPassword(email, newPassword).addOnCompleteListener { signInTask ->
                            if (signInTask.isSuccessful) {
                                val user = auth.currentUser
                                user?.updatePassword(newPassword)
                                    ?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val userId = user.uid
                                            val userData = hashMapOf(
                                                "password" to newPassword
                                            )

                                            db.collection("users").document(userId)
                                                .update(userData as Map<String, Any>)
                                                .addOnSuccessListener {
                                                    Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener { exception ->
                                                    Toast.makeText(this, "Failed to update password in Firestore: ${exception.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        } else {
                                            Toast.makeText(this, "Failed to update password in Firebase Authentication: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(this, "Failed to sign in with new password: ${signInTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    newPasswordDialog.setNegativeButton("Cancel") { dialog, which ->
                        dialog.cancel()
                    }

                    newPasswordDialog.show()
                } else {
                    Toast.makeText(this, "Failed to send password reset email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }


    }
}
