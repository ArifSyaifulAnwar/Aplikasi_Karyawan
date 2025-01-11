package com.example.tunasid.karyawan

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tunasid.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException

class editProfil : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var profileImageView: ImageView
    private var selectedImageUri: Uri? = null
    private val REQUEST_CAMERA = 100
    private val REQUEST_GALLERY = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profil)
        profileImageView = findViewById(R.id.profileImage)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailText)
        val passwordEditText = findViewById<EditText>(R.id.passwordText)
        val saveChangesButton = findViewById<Button>(R.id.saveButton)
        val passwordToggle = findViewById<ImageView>(R.id.showPasswordButton)

        // Tampilkan email pengguna saat ini
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val email = currentUser.email ?: "Email tidak ditemukan"
            emailEditText.setText(email)

            // Ambil password dari Firestore
            fetchPassword(currentUser.uid, passwordEditText)

            // Toggle visibility untuk password
            passwordToggle.setOnClickListener {
                togglePasswordVisibility(passwordEditText, passwordToggle)
            }

            profileImageView.setOnClickListener {
                showImagePickerOptions()
            }

            // Simpan perubahan password
            saveChangesButton.setOnClickListener {
                val newPassword = passwordEditText.text.toString()
                val newEmail = emailEditText.text.toString()
                saveChanges(currentUser.uid, newEmail, newPassword)
            }
        } else {
            Toast.makeText(this, "Pengguna tidak ditemukan. Harap login ulang.", Toast.LENGTH_SHORT).show()
        }


        profileImageView = findViewById(R.id.profileImage)

        profileImageView.setOnClickListener {
            showImagePickerOptions()
        }
    }
    private fun saveChanges(userId: String, newEmail: String, newPassword: String) {
        if (newPassword.isEmpty() || newPassword.length < 6) {
            Toast.makeText(this, "Password harus memiliki minimal 6 karakter", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri != null) {
            uploadProfileImage(userId, selectedImageUri!!) { imageUrl ->
                updateUserProfile(userId, newEmail, newPassword, imageUrl)
            }
        } else if (selectedImageUri == null) {
            Toast.makeText(this, "Harap pilih gambar sebelum menyimpan perubahan.", Toast.LENGTH_SHORT).show()
            return
        }

        else {
            updateUserProfile(userId, newEmail, newPassword, null)
        }
    }
    private fun uploadProfileImage(userId: String, imageUri: Uri, callback: (String) -> Unit) {
        val storageRef = storage.reference.child("profile_images/$userId.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString()) // Mengembalikan URL gambar yang diunggah
                    Toast.makeText(this, "Gambar berhasil diunggah: $uri", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal mendapatkan URL gambar: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengunggah foto profil: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserProfile(userId: String, email: String, password: String, profileImageUrl: String?) {
        val updates = mutableMapOf<String, Any>(
            "email" to email,
            "password" to password
        )

        if (profileImageUrl != null) {
            updates["profileImage"] = profileImageUrl
            Toast.makeText(this, "URL Gambar: $profileImageUrl", Toast.LENGTH_SHORT).show()
        }

        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan perubahan: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }





    private fun fetchPassword(userId: String, passwordEditText: EditText) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val password = document.getString("password")
                    if (password != null) {
                        passwordEditText.setText(password)
                    } else {
                        Toast.makeText(this, "Password tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat password: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun togglePasswordVisibility(passwordEditText: EditText, passwordToggle: ImageView) {
        val isPasswordVisible = passwordEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        if (isPasswordVisible) {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordToggle.setImageResource(R.drawable.eye)
        } else {
            passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordToggle.setImageResource(R.drawable.eye)
        }
        passwordEditText.setSelection(passwordEditText.text.length)
    }


    private fun showImagePickerOptions() {
        val options = arrayOf("Ambil Foto dari Kamera", "Pilih dari Galeri")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Pilih Opsi")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, REQUEST_CAMERA)
        }
    }

    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_GALLERY)
        } else {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, REQUEST_GALLERY)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(this, "Izin Kamera Diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    val photo: Bitmap = data?.extras?.get("data") as Bitmap
                    val tempUri = getImageUri(this, photo)
                    selectedImageUri = tempUri
                    profileImageView.setImageBitmap(photo)
                }
                REQUEST_GALLERY -> {
                    selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        try {
                            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                            profileImageView.setImageBitmap(bitmap)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(this, "Gagal Memuat Gambar", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    // Helper function untuk menyimpan bitmap dari kamera ke URI
    private fun getImageUri(context: Context, image: Bitmap): Uri {
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, image, "TempImage", null)
        return Uri.parse(path)
    }

}
