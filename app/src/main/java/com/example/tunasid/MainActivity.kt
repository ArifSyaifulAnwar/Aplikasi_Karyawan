package com.example.tunasid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button1 = findViewById<Button>(R.id.button_rekap)

        button1.setOnClickListener{
            val intent1 = Intent(this, DataRekap::class.java)
            startActivity(intent1)
        }

    }
}