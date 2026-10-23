package com.example.docmate.presentation.main.confirmbooking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.docmate.databinding.ItemDayChipBinding

class DaysAdapter(
    private val days: List<DayUiModel>,
    private var selectedIndex: Int = -1,
    private val onDaySelected: (DayUiModel, Int) -> Unit
) : RecyclerView.Adapter<DaysAdapter.DayViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemDayChipBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position], position == selectedIndex)
    }

    override fun getItemCount(): Int = days.size

    fun selectDay(position: Int) {
        val previous = selectedIndex
        selectedIndex = position
        if (previous != -1) notifyItemChanged(previous)
        notifyItemChanged(selectedIndex)
    }

    inner class DayViewHolder(
        private val binding: ItemDayChipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(day: DayUiModel, isSelected: Boolean) {
            binding.tvDayName.text = day.dayName
            binding.tvDayNumber.text = day.dayNumber
            binding.layoutDayItem.isSelected = isSelected

            binding.root.setOnClickListener {
                onDaySelected(day, bindingAdapterPosition)
            }
        }
    }
}
