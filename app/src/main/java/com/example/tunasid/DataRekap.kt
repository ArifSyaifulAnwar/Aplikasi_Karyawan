package com.example.tunasid

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DataRekap : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_rekap)

        firestore = FirebaseFirestore.getInstance()

        val dataRekap : Button = findViewById(R.id.button_rekap)
        dataRekap.setOnClickListener {
            startActivity(Intent(this, data_rekap_gabung::class.java))
        }
        val folderTepatWaktu : RelativeLayout = findViewById(R.id.folder_tepat_waktu)
        folderTepatWaktu.setOnClickListener {
            startActivity(Intent(this, BulanTepatWaktu::class.java))
        }
        val folderTerlambat : RelativeLayout = findViewById(R.id.folder_terlambat)
        folderTerlambat.setOnClickListener {
            startActivity(Intent(this, terlambat::class.java))
        }
        val pilihanTepatWaktu: ImageView = findViewById(R.id.pilihan_tepat_waktu)
        pilihanTepatWaktu.setOnClickListener { showPopupMenu(it, "Tepat Waktu") }

        val pilihanTerlambat: ImageView = findViewById(R.id.pilihan_terlambat)
        pilihanTerlambat.setOnClickListener { showPopupMenu(it, "Terlambat") }
    }

    private fun showPopupMenu(view: View, kategori: String) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.cetak_excel -> rekapData(kategori)
                // Tambahkan menu lain jika diperlukan
                else -> false
            }
            true
        }

        popupMenu.show()
    }

    private fun rekapData(kategori: String) {
        firestore.collection("absensi")
            // Mengambil semua data tanpa filter berdasarkan status
            .whereEqualTo("keterlambatan", kategori) // Filter berdasarkan kategori
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Tidak ada data untuk kategori $kategori.", Toast.LENGTH_SHORT).show()
                    Log.d("FirestoreDebug", "No documents found for category: $kategori")
                } else {
                    Log.d("FirestoreDebug", "Documents found for category: $kategori")
                    val data = documents.map { it.data }
                    createExcelFile(kategori, data)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal mengambil data.", Toast.LENGTH_SHORT).show()
                Log.e("FirestoreDebug", "Error getting documents: ", exception)
            }
    }

    private fun createExcelFile(kategori: String, data: List<Map<String, Any>>) {
        val workbook = HSSFWorkbook()
        val sheet = workbook.createSheet("Rekap $kategori")
        var rowIndex = 0

        // Header
        val headerRow: Row = sheet.createRow(rowIndex++)
        val headers = listOf("No", "Nama", "Keterangan", "Tanggal", "Waktu")
        for ((index, header) in headers.withIndex()) {
            val cell: Cell = headerRow.createCell(index)
            cell.setCellValue(header)
        }

        // Sort data by timestamp
        val sortedData = data.sortedBy { record ->
            val timestamp = record["timestamp"] as? com.google.firebase.Timestamp
            timestamp?.toDate()
        }

        // Data Rows
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        var noUrut = 1 // Nomor urut dimulai dari 2
        for (record in sortedData) {
            val row: Row = sheet.createRow(rowIndex++)

            row.createCell(0).setCellValue(noUrut.toDouble()) // Nomor urut
            row.createCell(1).setCellValue(record["name"] as? String ?: "N/A")
            row.createCell(2).setCellValue(record["keterlambatan"] as? String ?: "N/A")

            val timestamp = record["timestamp"] as? com.google.firebase.Timestamp

            // Memisahkan tanggal dan waktu
            val formattedDate = timestamp?.toDate()?.let { dateFormat.format(it) } ?: "N/A"
            val formattedTime = timestamp?.toDate()?.let { timeFormat.format(it) } ?: "N/A"

            row.createCell(3).setCellValue(formattedDate) // Tanggal
            row.createCell(4).setCellValue(formattedTime) // Waktu

            noUrut++ // Increment nomor urut
        }

        // Save the file
        val fileName = "rekap_data_${kategori.toLowerCase()}_${System.currentTimeMillis()}.xls"
        val file = File(getExternalFilesDir(null), fileName)

        try {
            FileOutputStream(file).use { output ->
                workbook.write(output)
                workbook.close()
            }

            // Show success message
            Toast.makeText(this, "Rekap $kategori berhasil disimpan: ${file.absolutePath}", Toast.LENGTH_LONG).show()

            // Open the Excel file
            openExcelFile(file)

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal menyimpan file Excel.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun openExcelFile(file: File) {
        val uri: Uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/vnd.ms-excel")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Tidak ada aplikasi untuk membuka file Excel.", Toast.LENGTH_SHORT).show()
        }
    }
}