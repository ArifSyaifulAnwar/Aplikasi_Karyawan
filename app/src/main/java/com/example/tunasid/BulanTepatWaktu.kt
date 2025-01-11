package com.example.tunasid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList
import android.graphics.Color
import android.widget.ImageView

class BulanTepatWaktu : AppCompatActivity() {
    private lateinit var bulanViews: Array<TextView>
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bulan_tepat_waktu)

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


        // Hitung tanggal awal dan akhir bulan yang dipilih
        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth - 1, 1, 0, 0, 0) // Bulan dimulai dari 0
        val startDate = Timestamp(calendar.time)

        calendar.set(selectedYear, selectedMonth - 1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        val endDate = Timestamp(calendar.time)

        db.collection("absensi")
            .whereGreaterThanOrEqualTo("timestamp", startDate)
            .whereLessThanOrEqualTo("timestamp", endDate)
            .whereEqualTo("keterlambatan", "Tepat Waktu") // Filter hanya "Tepat Waktu"
            .get()
            .addOnSuccessListener { documents ->
                val dataList = ArrayList<String>()
                if (documents.isEmpty) {
                    // Jika tidak ada data
                    dataList.add("Belum ada data di bulan $selectedMonth $selectedYear.")
                } else {
                    // Jika ada data, masukkan semua dokumen ke dalam daftar
                    for (document in documents) {
                        val name = document.getString("name")
                        val keterlambatan = document.getString("keterlambatan")
                        val timestamp = document.getTimestamp("timestamp")?.toDate()

                        dataList.add("Name: $name, Keterangan: $keterlambatan, Tanggal: $timestamp")
                    }
                }
                // Kirim data ke activity berikutnya
                val intent = Intent(this, mingguan::class.java)
                intent.putStringArrayListExtra("dataList", dataList)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                if (e.message?.contains("FAILED_PRECONDITION") == true) {
                    Toast.makeText(
                        this,
                        "Error: Index Firestore belum dibuat. Silakan buat index di Firestore console.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }



}
