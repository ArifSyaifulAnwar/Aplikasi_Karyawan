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
import java.util.ArrayList
import java.util.Calendar

class terlambat : AppCompatActivity() {
    private lateinit var bulanViews: Array<TextView>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_terlambat)
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

        // Membuat ArrayAdapter untuk Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearsList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = adapter

        // Set default ke tahun saat ini
        yearSpinner.setSelection(yearsList.indexOf(currentYear.toString()))

        // Listener untuk menangani pemilihan tahun
        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedYear = parent.getItemAtPosition(position).toString()
                // Tampilkan tahun yang dipilih, Anda bisa menggantinya dengan logika yang sesuai
                Toast.makeText(this@terlambat, "Tahun dipilih: $selectedYear", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing here
            }
        }


        val buttonBack = findViewById<ImageView>(R.id.imageView4)
        buttonBack.setOnClickListener{
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
        for (bulanView in bulanViews) {
            bulanView.setOnClickListener {
                changeSelectedMonth(it)
                openMingguanActivity()
            }
        }
    }

    private fun changeSelectedMonth(selectedMonth: View) {

        for (bulanView in bulanViews) {
            bulanView.setBackgroundResource(R.drawable.box_background_white) // Ganti ke background default
            (bulanView as TextView).setTextColor(Color.BLACK) // Ganti ke warna teks default
        }

        // Mengubah warna bulan yang dipilih
        selectedMonth.setBackgroundResource(R.drawable.box_background) // Ganti ke background hijau
        (selectedMonth as TextView).setTextColor(Color.WHITE) // Ganti ke warna teks putih
    }

    private fun openMingguanActivity() {
        val intent = Intent(this, list_terlambat::class.java)
        startActivity(intent) // Mulai aktivitas Mingguan

    }
}