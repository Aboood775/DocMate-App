package com.example.docmate.presentation.main.booking.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.docmate.R
import com.example.docmate.data.model.Appointment
import com.example.docmate.data.model.AppointmentStatus
import com.example.docmate.databinding.ItemBookingBinding

class BookingAdapter(
    private val onBookingClick: (Appointment) -> Unit
) : ListAdapter<Appointment, BookingAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookingViewHolder(binding, onBookingClick)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookingViewHolder(
        private val binding: ItemBookingBinding,
        private val onBookingClick: (Appointment) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: Appointment) {
            val context = binding.root.context

            binding.tvDoctorName.text = appointment.doctorName
            binding.tvDoctorSpecialty.text = appointment.doctorSpecialty
            binding.tvAppointmentDate.text = appointment.date
            binding.tvAppointmentTime.text = appointment.time
            binding.tvDoctorInitials.text = appointment.doctorInitials

            val (badgeBg, badgeTextColor, statusText) = when (appointment.status) {
                AppointmentStatus.CONFIRMED -> Triple(
                    R.color.colorConfirmedBg,
                    R.color.colorSuccess,
                    "Confirmed"
                )
                AppointmentStatus.PENDING -> Triple(
                    R.color.colorPendingBg,
                    R.color.colorWarning,
                    "Pending"
                )
                AppointmentStatus.CANCELLED -> Triple(
                    R.color.colorCancelledBg,
                    R.color.colorError,
                    "Cancelled"
                )
                AppointmentStatus.COMPLETED -> Triple(
                    R.color.colorDivider,
                    R.color.colorTextSecondary,
                    "Completed"
                )
            }

            binding.tvStatusBadge.text = statusText
            binding.tvStatusBadge.visibility = View.VISIBLE
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, badgeTextColor))
            binding.tvStatusBadge.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, badgeBg))

            binding.root.setOnClickListener {
                onBookingClick(appointment)
            }
        }
    }

    class BookingDiffCallback : DiffUtil.ItemCallback<Appointment>() {
        override fun areItemsTheSame(oldItem: Appointment, newItem: Appointment): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Appointment, newItem: Appointment): Boolean =
            oldItem == newItem
    }
}
