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
import java.util.Calendar

class data_rekap_gabung : AppCompatActivity() {
    private lateinit var bulanViews: Array<TextView>
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_rekap_gabung)

        // Setup padding untuk insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val yearSpinner: Spinner = findViewById(R.id.yearSpinner)
        val yearsList = ArrayList<String>()

        // Mengisi daftar tahun
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        for (i in currentYear - 10..currentYear + 10) {
            yearsList.add(i.toString())
        }

        // Adapter untuk Spinner
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

        // Mengatur klik pada tombol back
        val buttonBack = findViewById<ImageView>(R.id.imageView4)
        buttonBack.setOnClickListener {
            startActivity(Intent(this, DataRekap::class.java)) // Kembali ke activity sebelumnya
        }

        // Mendapatkan TextView bulan
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

        // Listener untuk tiap bulan
        for ((index, bulanView) in bulanViews.withIndex()) {
            bulanView.setOnClickListener {
                changeSelectedMonth(it)
                openMingguanActivity(selectedYear, index + 1) // Index + 1 karena bulan dimulai dari 1
            }
        }
    }

    private fun changeSelectedMonth(selectedMonth: View) {
        // Reset semua bulan ke status default
        for (bulanView in bulanViews) {
            bulanView.setBackgroundResource(R.drawable.box_background_white_gabung) // Background default
            (bulanView as TextView).setTextColor(Color.BLACK) // Warna teks default
        }
        // Set bulan yang dipilih
        selectedMonth.setBackgroundResource(R.drawable.box_background_gabing) // Ganti background
        (selectedMonth as TextView).setTextColor(Color.WHITE) // Ganti warna teks
    }

    private fun openMingguanActivity(selectedYear: Int, selectedMonth: Int) {
        val db = FirebaseFirestore.getInstance()

        // Hitung tanggal awal dan akhir bulan yang dipilih
        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth - 1, 1, 0, 0, 0) // Set ke hari pertama bulan, bulan dimulai dari 0
        val startDate = Timestamp(calendar.time)

        calendar.set(selectedYear, selectedMonth - 1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        val endDate = Timestamp(calendar.time)

        db.collection("absensi")
            .whereGreaterThanOrEqualTo("timestamp", startDate)
            .whereLessThanOrEqualTo("timestamp", endDate)
            .whereIn("keterlambatan", listOf("Tepat Waktu", "Terlambat")) // Filter untuk "Tepat Waktu" dan "Terlambat"
            .get()
            .addOnSuccessListener { documents ->
                val dataList = ArrayList<String>()
                if (documents.isEmpty) {
                    dataList.add("Belum ada data di bulan $selectedMonth $selectedYear.")
                } else {
                    for (document in documents) {
                        val name = document.getString("name")
                        val keterlambatan = document.getString("keterlambatan")
                        val timestamp = document.getTimestamp("timestamp")?.toDate()
                        dataList.add("Nama: $name, Status: $keterlambatan, Tanggal: $timestamp")
                    }
                }
                val intent = Intent(this, data_rekap_gabung_mingguan::class.java)
                intent.putStringArrayListExtra("dataList", dataList)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                if (e.message?.contains("FAILED_PRECONDITION") == true) {
                    Toast.makeText(
                        this,
                        "Error: Index Firestore belum dibuat untuk query ini. Silakan buat index di Firestore console.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

}