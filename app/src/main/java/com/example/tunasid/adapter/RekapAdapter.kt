package com.example.tunasid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tunasid.R

class RekapAdapter(private val dataList: List<RekapItem>) :
    RecyclerView.Adapter<RekapAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.namaKaryawan)
        val tvPosition: TextView = view.findViewById(R.id.positionKaryawan)
        val tvTotal: TextView = view.findViewById(R.id.total)
        val tvMonday: TextView = view.findViewById(R.id.senin)
        val tvTuesday: TextView = view.findViewById(R.id.selasa)
        val tvWednesday: TextView = view.findViewById(R.id.rabu)
        val tvThursday: TextView = view.findViewById(R.id.kamis)
        val tvFriday: TextView = view.findViewById(R.id.jumat)
        val tvSaturday : TextView = view.findViewById(R.id.sabtu)
        // Tambahkan TextView lainnya untuk hari lainnya
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardmingguan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.tvName.text = item.name
        holder.tvPosition.text = item.position
        holder.tvTotal.text = item.total
        holder.tvMonday.text = item.monday
        holder.tvTuesday.text = item.tuesday
        holder.tvWednesday.text = item.wednesday
        holder.tvThursday.text = item.thursday
        holder.tvFriday.text = item.friday
        holder.tvSaturday.text = item.saturday
        // Set data lainnya
    }

    override fun getItemCount(): Int = dataList.size
}

data class RekapItem(
    val name: String,
    val position: String,
    val total: String,
    val monday: String,
    val tuesday: String,
    val wednesday: String,
    val thursday: String,
    val friday: String,
    val saturday: String,
    // Tambahkan hari lainnya
)
