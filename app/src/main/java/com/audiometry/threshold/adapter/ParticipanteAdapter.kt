package com.audiometry.threshold.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.audiometry.threshold.R
import com.audiometry.threshold.database.entity.Participant

class ParticipanteAdapter(
    private val onSelectionChanged: (List<Participant>) -> Unit,
    private val onActionClick: (Participant, View) -> Unit
) : RecyclerView.Adapter<ParticipanteAdapter.VH>() {

    private var items: List<Participant> = emptyList()
    private val selectedIds = mutableSetOf<Int>()

    fun submitList(list: List<Participant>) {
        items = list
        selectedIds.retainAll(list.map { it.id }.toSet())
        notifyDataSetChanged()
    }

    fun selectAll(select: Boolean) {
        if (select) selectedIds.addAll(items.map { it.id }) else selectedIds.clear()
        notifyDataSetChanged()
        onSelectionChanged(getSelected())
    }

    fun isAllSelected() = items.isNotEmpty() && selectedIds.containsAll(items.map { it.id })

    fun getSelected(): List<Participant> = items.filter { it.id in selectedIds }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val cb: CheckBox = v.findViewById(R.id.cbSelect)
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvTest: TextView = v.findViewById(R.id.tvTestDone)
        val tvDate: TextView = v.findViewById(R.id.tvLastTestDate)
        val tvStatus: TextView = v.findViewById(R.id.tvAudioStatus)
        val tvReeval: TextView = v.findViewById(R.id.tvNeedsReeval)
        val btnActions: ImageButton = v.findViewById(R.id.btnActions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_participante, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.tvName.text = p.nomeCompleto
        holder.tvDate.text = p.dataUltimoTeste.ifEmpty { "-" }
        holder.tvReeval.text = p.necessitaReavaliacao
        holder.tvTest.text = p.testeRealizado
        holder.tvTest.setBackgroundResource(
            if (p.testeRealizado == "Aud.IT") R.drawable.pill_audit else R.drawable.pill_ouvir
        )
        holder.tvStatus.text = p.statusAuditivo
        holder.tvStatus.setBackgroundResource(
            if (p.statusAuditivo == "Normal") R.drawable.pill_status_normal else R.drawable.pill_status_alterado
        )
        holder.cb.setOnCheckedChangeListener(null)
        holder.cb.isChecked = p.id in selectedIds
        holder.cb.setOnCheckedChangeListener { _, checked ->
            if (checked) selectedIds.add(p.id) else selectedIds.remove(p.id)
            onSelectionChanged(getSelected())
        }
        holder.btnActions.setOnClickListener { onActionClick(p, it) }
    }

    override fun getItemCount() = items.size
}
