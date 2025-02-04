package com.example.tunasid

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList
import java.util.Calendar
class terlambat : AppCompatActivity() {
    private lateinit var bulanViews: Array<TextView>
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terlambat)

        val yearSpinner: Spinner = findViewById(R.id.yearSpinner)
        val yearsList = ArrayList<String>()

        // Mengisi daftar tahun
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        for (i in currentYear - 10..currentYear + 10) {
            yearsList.add(i.toString())
        }

        // Membuat ArrayAdapter untuk Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearsList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = adapter

        // Set default ke tahun saat ini
        yearSpinner.setSelection(yearsList.indexOf(currentYear.toString()))

        // Listener untuk menangani pemilihan tahun
        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedYear = parent.getItemAtPosition(position).toString().toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val buttonBack = findViewById<ImageView>(R.id.imageView4)
        buttonBack.setOnClickListener {
            val intent1 = Intent(this, DataRekap::class.java)
            startActivity(intent1)
        }

        bulanViews = arrayOf(
            findViewById(R.id.jan),
            findViewById(R.id.feb),
            findViewById(R.id.mar),
            findViewById(R.id.apr),
            findViewById(R.id.mei),
            findViewById(R.id.jun),
            findViewById(R.id.jul),
            findViewById(R.id.agus),
            findViewById(R.id.sep),
            findViewById(R.id.okt),
            findViewById(R.id.nov),
            findViewById(R.id.desc)
        )

        // Menetapkan listener pada setiap TextView
        for ((index, bulanView) in bulanViews.withIndex()) {
            bulanView.setOnClickListener {
                changeSelectedMonth(it)
                openMingguanActivity(selectedYear, index + 1) // Index + 1 karena bulan dimulai dari 1
            }
        }
    }

    private fun changeSelectedMonth(selectedMonth: View) {
        for (bulanView in bulanViews) {
            bulanView.setBackgroundResource(R.drawable.box_background_white) // Background default
            (bulanView as TextView).setTextColor(Color.BLACK) // Warna teks default
        }

        // Mengubah warna bulan yang dipilih
        selectedMonth.setBackgroundResource(R.drawable.box_background) // Background hijau
        (selectedMonth as TextView).setTextColor(Color.WHITE) // Warna teks putih
    }


    private fun openMingguanActivity(selectedYear: Int, selectedMonth: Int) {
        val db = FirebaseFirestore.getInstance()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, selectedYear)
        calendar.set(Calendar.MONTH, selectedMonth - 1) // Bulan dimulai dari 0
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = Timestamp(calendar.time)

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = Timestamp(calendar.time)

        println("Querying data for month: $selectedMonth, year: $selectedYear")
        println("Start Date: $startDate")
        println("End Date: $endDate")

        // Kirim data ke activity berikutnya tanpa query Firestore di sini
        val intent = Intent(this, list_terlambat::class.java)
        intent.putExtra("selectedYear", selectedYear)
        intent.putExtra("selectedMonth", selectedMonth)
        intent.putExtra("startDate", startDate.seconds * 1000) // Convert to milliseconds
        intent.putExtra("endDate", endDate.seconds * 1000)    // Convert to milliseconds
        startActivity(intent)
    }




}
