package com.audiometry.threshold

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class LogAdapter : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {
    
    private val logEntries = mutableListOf<LogEntry>()
    
    fun addEntry(entry: LogEntry) {
        logEntries.add(entry)
        notifyItemInserted(logEntries.size - 1)
    }
    
    fun clear() {
        logEntries.clear()
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log, parent, false)
        return LogViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(logEntries[position])
    }
    
    override fun getItemCount() = logEntries.size
    
    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTrial: TextView = itemView.findViewById(R.id.tvLogTrial)
        private val tvDb: TextView = itemView.findViewById(R.id.tvLogDb)
        private val tvIsi: TextView = itemView.findViewById(R.id.tvLogIsi)
        private val tvWin: TextView = itemView.findViewById(R.id.tvLogWin)
        private val tvTrack: TextView = itemView.findViewById(R.id.tvLogTrack)
        private val tvResponse: TextView = itemView.findViewById(R.id.tvLogResponse)
        private val tvStat: TextView = itemView.findViewById(R.id.tvLogStat)
        
        fun bind(entry: LogEntry) {
            tvTrial.text = entry.trial.toString()
            tvDb.text = entry.db.toString()
            tvIsi.text = "${entry.isi}ms"
            tvWin.text = "${entry.window}ms"
            tvTrack.text = entry.track
            
            val context = itemView.context
            if (entry.heard) {
                tvResponse.text = context.getString(R.string.heard_response)
                tvResponse.setTextColor(ContextCompat.getColor(context, R.color.color_ok))
            } else {
                tvResponse.text = context.getString(R.string.not_heard_response)
                tvResponse.setTextColor(ContextCompat.getColor(context, R.color.color_bad))
            }
            
            tvStat.text = entry.stat
        }
    }
}
