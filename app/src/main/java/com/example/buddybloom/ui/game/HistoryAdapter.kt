package com.example.buddybloom.ui.game

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.buddybloom.R
import com.example.buddybloom.data.model.PlantHistory
import com.google.android.material.textview.MaterialTextView

class HistoryAdapter(private val historyItems: MutableList<PlantHistory>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: MaterialTextView = itemView.findViewById(R.id.tv_history_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return historyItems.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentItem = historyItems[position]
        val textString =
            "You managed to keep your ${currentItem.name} alive for ${currentItem.streakCount} days!"
        holder.textView.text = textString
    }

    fun update(newItems: List<PlantHistory>) {
        historyItems.clear()
        historyItems.addAll(newItems)
        notifyDataSetChanged()
    }
}