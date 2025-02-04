package com.example.tunasid

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class mingguan : AppCompatActivity() {
    private lateinit var weekViews: Array<TextView>
    private lateinit var dataContainer: LinearLayout
    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var startDate: Long = 0
    private var endDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mingguan)
        selectedYear = intent.getIntExtra("selectedYear", Calendar.getInstance().get(Calendar.YEAR))
        selectedMonth = intent.getIntExtra("selectedMonth", Calendar.getInstance().get(Calendar.MONTH) + 1)
        startDate = intent.getLongExtra("startDate", 0)
        endDate = intent.getLongExtra("endDate", 0)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buttonBack = findViewById<ImageView>(R.id.imageView4)
        buttonBack.setOnClickListener {
            val intent1 = Intent(this, BulanTepatWaktu::class.java)
            startActivity(intent1)
        }

        weekViews = arrayOf(
            findViewById(R.id.pertama),
            findViewById(R.id.kedua),
            findViewById(R.id.ketiga),
            findViewById(R.id.keempat),
            findViewById(R.id.kelima)
        )

        dataContainer = findViewById(R.id.dataKaryawan)


        for ((index, weekView) in weekViews.withIndex()) {
            weekView.setOnClickListener {
                changeColor(it)
                loadDataForWeek(selectedYear, selectedMonth, index + 1)
            }
        }
    }

    private fun changeColor(selectedWeek: View) {
        // Reset colors for all week buttons
        for (weekView in weekViews) {
            weekView.setBackgroundResource(R.drawable.box_background_white) // Default background
            (weekView as TextView).setTextColor(Color.BLACK) // Default text color
        }

        // Highlight selected week
        selectedWeek.setBackgroundResource(R.drawable.box_background) // Selected background
        (selectedWeek as TextView).setTextColor(Color.WHITE) // Selected text color
    }


    private fun loadDataForWeek(selectedYear: Int, selectedMonth: Int, weekNumber: Int) {
        val db = FirebaseFirestore.getInstance()

        // Calculate week dates
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, selectedYear)
        calendar.set(Calendar.MONTH, selectedMonth - 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.WEEK_OF_MONTH, weekNumber)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val weekStartDate = Timestamp(calendar.time)

        calendar.add(Calendar.DATE, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)

        val weekEndDate = Timestamp(calendar.time)

        // Make sure we're within the selected month's bounds
        val monthStartDate = Timestamp(Date(startDate))
        val monthEndDate = Timestamp(Date(endDate))

        // Use the more restrictive of the two date ranges
        val queryStartDate = if (weekStartDate.seconds < monthStartDate.seconds) monthStartDate else weekStartDate
        val queryEndDate = if (weekEndDate.seconds > monthEndDate.seconds) monthEndDate else weekEndDate

        db.collection("absensi")
            .whereGreaterThanOrEqualTo("timestamp", queryStartDate)
            .whereLessThanOrEqualTo("timestamp", queryEndDate)
            .whereEqualTo("keterlambatan", "Tepat Waktu")
            .get()
            .addOnSuccessListener { documents ->
                val weeklyData = mutableListOf<AbsensiKaryawan>()
                for (document in documents) {
                    val name = document.getString("name") ?: "Unknown"
                    val keterlambatan = document.getString("keterlambatan") ?: "Unspecified"
                    val timestamp = document.getTimestamp("timestamp")?.toDate() ?: Date()

                    // Only add data that falls within the selected month
                    if (timestamp.time in startDate..endDate) {
                        weeklyData.add(AbsensiKaryawan(name, keterlambatan, timestamp))
                    }
                }
                displayWeekData(weeklyData)
            }
            .addOnFailureListener { e ->
                showError("Error retrieving data: ${e.message}")
            }
    }



    private fun displayWeekData(weeklyData: List<AbsensiKaryawan>) {
        val dayIdMap = mapOf(
            Calendar.MONDAY to R.id.senin,
            Calendar.TUESDAY to R.id.selasa,
            Calendar.WEDNESDAY to R.id.rabu,
            Calendar.THURSDAY to R.id.kamis,
            Calendar.FRIDAY to R.id.jumat,
            Calendar.SATURDAY to R.id.sabtu
        )

        dataContainer.removeAllViews() // Clear existing views

        if (weeklyData.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "Belum ada data untuk minggu ini."
                textSize = 16f
                setTextColor(Color.GRAY)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            dataContainer.addView(emptyView)
            return
        }

        // Group data by employee name
        weeklyData.groupBy { it.name }.forEach { (name, entries) ->
            val cardView = LayoutInflater.from(this).inflate(R.layout.cardmingguan, dataContainer, false)
            val nameTextView = cardView.findViewById<TextView>(R.id.namaKaryawan)
            val positionTextView = cardView.findViewById<TextView>(R.id.positionKaryawan)
            val totalTextView = cardView.findViewById<TextView>(R.id.total)
            val profileImageView = cardView.findViewById<ShapeableImageView>(R.id.fotoprofil) // Tambahkan ini
            var buttonCount = 0

            // Set nama dan jabatan
            nameTextView?.text = name
            positionTextView?.text = "Karyawan"

            // Memuat foto profil dari Firebase
            fetchProfileImage(name, profileImageView)

            // Populate days in the week
            entries.forEach { entry ->
                val calendar = Calendar.getInstance().apply { time = entry.timestamp }
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val dayTextViewId = dayIdMap[dayOfWeek]
                val dayTextView = cardView.findViewById<TextView>(dayTextViewId ?: 0)

                dayTextView?.let {
                    it.visibility = View.VISIBLE
                    it.text = SimpleDateFormat("EEE, dd HH:mm", Locale("id", "ID")).format(entry.timestamp)
                    it.background = getDrawable(R.drawable.box_background)
                    buttonCount++
                }
            }
            totalTextView.text = "$buttonCount/6"
            dataContainer.addView(cardView)
        }
    }

    private fun fetchProfileImage(name: String, imageView: ShapeableImageView) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("nama", name) // Ambil berdasarkan nama karyawan
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val profileImageUrl = documents.documents[0].getString("profileImage")
                    if (!profileImageUrl.isNullOrEmpty()) {
                        // Memuat gambar menggunakan Glide
                        Glide.with(this)
                            .load(profileImageUrl)
                            //.placeholder(R.drawable.default_profile_placeholder) // Placeholder jika loading
                            .error(R.drawable.fotoprofil) // Placeholder jika gagal
                            .into(imageView)
                    } else {
                        // Gambar tidak tersedia, gunakan default
                        imageView.setImageResource(R.drawable.fotoprofil)
                    }
                } else {
                    // Tidak ada dokumen, gunakan gambar default
                    imageView.setImageResource(R.drawable.fotoprofil)
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                // Jika terjadi error, gunakan gambar default
                imageView.setImageResource(R.drawable.fotoprofil)
            }
    }




    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
