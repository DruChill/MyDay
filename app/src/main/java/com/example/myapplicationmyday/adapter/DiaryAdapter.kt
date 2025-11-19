package com.example.myapplicationmyday.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationmyday.data.DiaryEntry
import com.example.myapplicationmyday.databinding.ItemDiaryEntryBinding
import java.text.SimpleDateFormat
import java.util.*

class DiaryAdapter(
    private val onItemClick: (DiaryEntry) -> Unit
) : ListAdapter<DiaryEntry, DiaryAdapter.DiaryViewHolder>(DiaryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val binding = ItemDiaryEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DiaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DiaryViewHolder(
        private val binding: ItemDiaryEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(entry: DiaryEntry) {
            binding.tvEntryTitle.text = entry.title.ifEmpty { "Sin título" }
            binding.tvEntryContent.text = entry.content
            binding.tvEntryDate.text = formatDate(entry.date)
        }

        private fun formatDate(timestamp: Long): String {
            val date = Date(timestamp)
            val now = Calendar.getInstance()
            val entryDate = Calendar.getInstance().apply { time = date }

            return when {
                isSameDay(now, entryDate) -> "Hoy"
                isYesterday(now, entryDate) -> "Ayer"
                else -> {
                    val dateFormat = SimpleDateFormat("d MMM yyyy", Locale("es", "ES"))
                    dateFormat.format(date)
                }
            }
        }

        private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }

        private fun isYesterday(now: Calendar, date: Calendar): Boolean {
            val yesterday = now.clone() as Calendar
            yesterday.add(Calendar.DAY_OF_YEAR, -1)
            return isSameDay(yesterday, date)
        }
    }

    class DiaryDiffCallback : DiffUtil.ItemCallback<DiaryEntry>() {
        override fun areItemsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry): Boolean {
            return oldItem == newItem
        }
    }
}
