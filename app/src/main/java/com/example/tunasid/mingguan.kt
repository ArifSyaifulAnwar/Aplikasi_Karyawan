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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class mingguan : AppCompatActivity() {
    private lateinit var weekViews: Array<TextView>
    private lateinit var dataContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mingguan)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back button
        val buttonBack = findViewById<ImageView>(R.id.imageView4)
        buttonBack.setOnClickListener {
            val intent1 = Intent(this, BulanTepatWaktu::class.java)
            startActivity(intent1)
        }

        // Week buttons
        weekViews = arrayOf(
            findViewById(R.id.pertama),
            findViewById(R.id.kedua),
            findViewById(R.id.ketiga),
            findViewById(R.id.keempat)
        )

        // Container for dynamic views
        dataContainer = findViewById(R.id.dataKaryawan)

        // Handle week selection
        for ((index, weekView) in weekViews.withIndex()) {
            weekView.setOnClickListener {
                changeColor(it)
                val selectedYear = intent.getIntExtra("year", Calendar.getInstance().get(Calendar.YEAR))
                val selectedMonth = intent.getIntExtra("month", Calendar.getInstance().get(Calendar.MONTH) + 1)
                loadDataForWeek(selectedYear, selectedMonth, index + 1) // Week starts from 1
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

        // Setting up the calendar for the specified week
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, selectedYear)
        calendar.set(Calendar.MONTH, selectedMonth - 1) // Month is zero-based in Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.WEEK_OF_MONTH, weekNumber)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startDate = Timestamp(calendar.time)

        calendar.add(Calendar.DATE, 6) // End date is 6 days after start date (full week)
        val endDate = Timestamp(calendar.time)

        // Query Firestore to fetch only "Tepat Waktu" records
        db.collection("absensi")
            .whereGreaterThanOrEqualTo("timestamp", startDate)
            .whereLessThanOrEqualTo("timestamp", endDate)
            .whereEqualTo("keterlambatan", "Tepat Waktu") // Only "Tepat Waktu" data
            .get()
            .addOnSuccessListener { documents ->
                val weeklyData = mutableListOf<AbsensiKaryawan>()
                for (document in documents) {
                    val name = document.getString("name") ?: "Unknown"
                    val keterlambatan = document.getString("keterlambatan") ?: "Unspecified"
                    val timestamp = document.getTimestamp("timestamp")?.toDate() ?: Date()
                    weeklyData.add(AbsensiKaryawan(name, keterlambatan, timestamp))
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

            nameTextView?.text = name
            positionTextView?.text = "Karyawan"

            // Populate days in the week
            entries.forEach { entry ->
                val calendar = Calendar.getInstance().apply { time = entry.timestamp }
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val dayTextViewId = dayIdMap[dayOfWeek]
                val dayTextView = cardView.findViewById<TextView>(dayTextViewId ?: 0)

                dayTextView?.let {
                    it.visibility = View.VISIBLE
                    it.text = SimpleDateFormat("EEE, dd HH:mm", Locale.getDefault()).format(entry.timestamp)
                    it.background = getDrawable(R.drawable.button_tepat_waktu)
                }
            }

            dataContainer.addView(cardView)
        }
    }


    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
