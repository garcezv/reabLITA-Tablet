package com.audiometry.threshold.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.audiometry.threshold.R
import com.audiometry.threshold.database.entity.Participant

class ConfirmParticipanteAdapter(
    private val items: MutableList<Participant>,
    private val onRemove: (Participant) -> Unit
) : RecyclerView.Adapter<ConfirmParticipanteAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val btnRemove: ImageButton = v.findViewById(R.id.btnRemoveParticipant)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_participante_confirm, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.tvName.text = p.nomeCompleto
        holder.btnRemove.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_ID.toInt()) {
                val removed = items[pos]
                items.removeAt(pos)
                notifyItemRemoved(pos)
                onRemove(removed)
            }
        }
    }

    override fun getItemCount() = items.size
    fun getItems(): List<Participant> = items.toList()
}
