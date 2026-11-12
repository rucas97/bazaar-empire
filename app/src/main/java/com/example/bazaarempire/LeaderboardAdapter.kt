package com.example.bazaarempire

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class LeaderboardAdapter(
    private val players: List<Triple<String, String, Int>>,
    private val currentPlayerName: String
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView = view.findViewById(R.id.tvRank)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = players[position]
        holder.tvRank.text = (position + 1).toString()
        holder.tvName.text = "${player.second}\n(${player.first})" 

        val timeStr = if (player.third == 9999) "--:--" else String.format(Locale.US, "%02d:%02d", player.third / 60, player.third % 60)
        holder.tvTime.text = timeStr

        if (player.second == currentPlayerName) {
            holder.tvName.setTextColor(Color.parseColor("#F4C430"))
            holder.tvName.setTypeface(null, Typeface.BOLD)
            holder.itemView.setBackgroundColor(Color.parseColor("#33FFFFFF"))
        } else {
            holder.tvName.setTextColor(Color.parseColor("#F5E6D3"))
            holder.tvName.setTypeface(null, Typeface.NORMAL)
            holder.itemView.setBackgroundColor(Color.parseColor("#1A4A4A"))
        }
    }

    override fun getItemCount() = players.size
}
