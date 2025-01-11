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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var a: FirebaseFirestore
    private lateinit var b: LinearLayout

    override fun onCreate(c: Bundle?) {
        super.onCreate(c)
        setContentView(R.layout.activity_main)

        val workRequest =
            OneTimeWorkRequestBuilder<CheckDataWorker>().build()

        WorkManager.getInstance(this).enqueue(workRequest)

        val d = findViewById<Button>(R.id.button_rekap)
        d.setOnClickListener {
            startActivity(Intent(this, DataRekap::class.java))
        }

        val e = findViewById<ImageView>(R.id.imageView3)
        e.setOnClickListener {
            startActivity(Intent(this, notifikasi::class.java))
        }

        b = findViewById(R.id.containerLayout)
        a = FirebaseFirestore.getInstance()

        f()
    }

    private fun f() {
        a.collection("absensi")
            .get()
            .addOnSuccessListener { g ->
                for (h in g) {
                    i(h.data)
                }
            }
            .addOnFailureListener { j ->
                Log.w("DB_ERR", "Error fetching data: ", j)
            }
    }

    private fun i(k: Map<String, Any>) {
        val l = layoutInflater.inflate(R.layout.attendance_card, null, false)
        val m: TextView = l.findViewById(R.id.nameTextView)
        val n: TextView = l.findViewById(R.id.roleTextView)
        val o: TextView = l.findViewById(R.id.statusTodayTextView)

        // Mendapatkan nilai dari data
        val name = k["name"] as? String ?: "Unknown"
        val keterlambatan = k["keterlambatan"] as? String ?: "Unknown"
        val timestamp = k["timestamp"] as? Timestamp

        // Mformat tanggal dan waktu
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedDate = timestamp?.toDate()?.let {
            "${dateFormat.format(it)} - ${timeFormat.format(it)}"
        } ?: "Unknown Time"

        m.text = name
        n.text = "Staff"
        o.text = "tanggal: $formattedDate"

        val buttonTepatWaktu: Button = l.findViewById(R.id.buttonTepatWaktu)
        val buttonTerlambat: Button = l.findViewById(R.id.buttonTerlambat)

        // Memeriksa status keterlambatan
        if (keterlambatan == "Terlambat") {
            buttonTepatWaktu.visibility = View.GONE
            buttonTerlambat.visibility = View.VISIBLE
        } else {
            buttonTepatWaktu.visibility = View.VISIBLE
            buttonTerlambat.visibility = View.GONE
        }

        // Penambahan layout parameter
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 40)
        l.layoutParams = layoutParams
        b.addView(l)
    }
}
