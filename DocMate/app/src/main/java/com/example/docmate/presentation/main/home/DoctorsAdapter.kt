package com.example.docmate.presentation.main.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.docmate.domain.model.Doctor
import com.example.docmate.databinding.ItemDoctorCardBinding

/**
 * Adapter لقائمة الدكاترة في شاشة Home.
 * بيستخدم ListAdapter + DiffUtil عشان التحديثات (نتيجة السيرش أو
 * الفلترة) تتحدث بسلاسة من غير ما نعيد رسم القائمة كاملة كل مرة.
 */
class DoctorsAdapter(
    private val onDoctorClick: (Doctor) -> Unit,
    private val onBookClick: (Doctor) -> Unit
) : ListAdapter<Doctor, DoctorsAdapter.DoctorViewHolder>(DoctorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val binding = ItemDoctorCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DoctorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DoctorViewHolder(
        private val binding: ItemDoctorCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(doctor: Doctor) {
            binding.tvDoctorInitials.text = doctor.initials()
            binding.tvDoctorName.text = doctor.name
            binding.tvDoctorSpecialty.text = doctor.specialty
            binding.tvDoctorRating.text = String.format("%.1f", doctor.rating)
            binding.tvAvailabilityBadge.visibility =
                if (doctor.availableToday) View.VISIBLE else View.GONE

            binding.root.setOnClickListener { onDoctorClick(doctor) }
            binding.btnBook.setOnClickListener { onBookClick(doctor) }
        }
    }

    private class DoctorDiffCallback : DiffUtil.ItemCallback<Doctor>() {
        override fun areItemsTheSame(oldItem: Doctor, newItem: Doctor): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Doctor, newItem: Doctor): Boolean =
            oldItem == newItem
    }
}
