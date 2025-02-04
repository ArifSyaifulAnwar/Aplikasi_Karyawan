package com.example.tunasid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var containerLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val workRequest = OneTimeWorkRequestBuilder<CheckDataWorker>().build()
        WorkManager.getInstance(this).enqueue(workRequest)

        val buttonRekap = findViewById<Button>(R.id.button_rekap)
        buttonRekap.setOnClickListener {
            startActivity(Intent(this, DataRekap::class.java))
        }
        val notip = findViewById<ImageView>(R.id.imageView3)
        notip.setOnClickListener {
            startActivity(Intent(this, notifikasi::class.java))
        }

        containerLayout = findViewById(R.id.containerLayout)
        firestore = FirebaseFirestore.getInstance()

        fetchAttendanceData()
    }

    private fun fetchAttendanceData() {
        firestore.collection("absensi")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("Firestore", "Document: ${document.data}")
                    renderAttendanceCard(document.data)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error fetching data: ", exception)
            }
    }

    private fun renderAttendanceCard(data: Map<String, Any>) {
        val attendanceCard = layoutInflater.inflate(R.layout.attendance_card, null, false)

        val nameTextView: TextView = attendanceCard.findViewById(R.id.nameTextView)
        val roleTextView: TextView = attendanceCard.findViewById(R.id.roleTextView)
        val statusTodayTextView: TextView = attendanceCard.findViewById(R.id.statusTodayTextView)
        val profileImageView: ShapeableImageView = attendanceCard.findViewById(R.id.profileImage)

        val name = data["name"] as? String ?: "Unknown"
        val keterlambatan = data["keterlambatan"] as? String ?: "Unknown"
        val timestamp = data["timestamp"] as? Timestamp

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedDate = timestamp?.toDate()?.let {
            "${dateFormat.format(it)} - ${timeFormat.format(it)}"
        } ?: "Unknown Time"

        nameTextView.text = name
        roleTextView.text = "Staff"
        statusTodayTextView.text = "tanggal: $formattedDate"

        val buttonTepatWaktu: Button = attendanceCard.findViewById(R.id.buttonTepatWaktu)
        val buttonTerlambat: Button = attendanceCard.findViewById(R.id.buttonTerlambat)

        if (keterlambatan == "Terlambat") {
            buttonTepatWaktu.visibility = View.GONE
            buttonTerlambat.visibility = View.VISIBLE
        } else {
            buttonTepatWaktu.visibility = View.VISIBLE
            buttonTerlambat.visibility = View.GONE
        }

        // Load profile image from Firestore users collection
        getUserProfileImage(name) { profileImageUrl ->
            if (profileImageUrl != null) {
                Glide.with(this)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.circle_background) // Default placeholder
                    .error(R.drawable.circle_background) // Default error image
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.circle_background)
            }
        }

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 40)
        attendanceCard.layoutParams = layoutParams
        containerLayout.addView(attendanceCard)
    }

    private fun getUserProfileImage(name: String, callback: (String?) -> Unit) {
        firestore.collection("users")
            .whereEqualTo("nama", name) // Match name field
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val document = result.documents[0]
                    val profileImageUrl = document.getString("profileImage")
                    Log.d("Firestore", "Profile image URL for $name: $profileImageUrl")
                    callback(profileImageUrl)
                } else {
                    Log.w("Firestore", "No user found with name: $name")
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error fetching user data for $name: ${exception.localizedMessage}")
                callback(null)
            }
    }
}
