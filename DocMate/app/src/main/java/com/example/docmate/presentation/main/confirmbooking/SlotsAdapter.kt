package com.example.docmate.presentation.main.confirmbooking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.docmate.domain.model.Slot
import com.example.docmate.databinding.ItemSlotChipBinding

class SlotsAdapter(
    private var slots: List<Slot> = emptyList(),
    private var selectedIndex: Int = -1,
    private val onSlotSelected: (Slot, Int) -> Unit
) : RecyclerView.Adapter<SlotsAdapter.SlotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val binding = ItemSlotChipBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SlotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        holder.bind(slots[position], position == selectedIndex)
    }

    override fun getItemCount(): Int = slots.size

    /** بيستبدل قائمة السلوتات بالكامل، مثلاً بعد ما اليوزر يغير اليوم المختار. */
    fun submitList(newSlots: List<Slot>) {
        slots = newSlots
        selectedIndex = -1
        notifyDataSetChanged()
    }

    fun clearSelection() {
        val previous = selectedIndex
        selectedIndex = -1
        if (previous != -1) notifyItemChanged(previous)
    }

    inner class SlotViewHolder(
        private val binding: ItemSlotChipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(slot: Slot, isSelected: Boolean) {
            binding.tvSlotTime.text = slot.time
            binding.tvSlotTime.isEnabled = slot.available
            binding.tvSlotTime.isSelected = isSelected

            binding.tvSlotTime.setOnClickListener {
                if (!slot.available) return@setOnClickListener
                val previous = selectedIndex
                selectedIndex = bindingAdapterPosition
                if (previous != -1) notifyItemChanged(previous)
                notifyItemChanged(selectedIndex)
                onSlotSelected(slot, selectedIndex)
            }
        }
    }
}
