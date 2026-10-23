package com.example.docmate.presentation.main.confirmbooking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.docmate.R
import com.example.docmate.databinding.FragmentConfirmBookingBinding
import com.google.android.material.snackbar.Snackbar
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class ConfirmBookingFragment : Fragment() {

    private var _binding: FragmentConfirmBookingBinding? = null
    private val binding get() = _binding!!

    private val args: ConfirmBookingFragmentArgs by navArgs()
    private val viewModel: ConfirmBookingViewModel by viewModels {
        ConfirmBookingViewModelFactory(args.doctorId)
    }

    private lateinit var daysAdapter: DaysAdapter
    private lateinit var slotsAdapter: SlotsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        setupDaysList()
        setupSlotsGrid()
        setupConfirmButton()
        observeViewModel()
    }

    private fun setupDaysList() {
        binding.rvDays.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        daysAdapter = DaysAdapter(
            days = viewModel.availableDays,
            onDaySelected = { day, _ ->
                viewModel.onDaySelected(day.date)
            }
        )
        binding.rvDays.adapter = daysAdapter
    }

    private fun setupSlotsGrid() {
        binding.rvSlots.layoutManager = LinearLayoutManager(requireContext())
        slotsAdapter = SlotsAdapter(
            onSlotSelected = { slot, _ ->
                viewModel.onSlotSelected(slot)
            }
        )
        binding.rvSlots.adapter = slotsAdapter
    }

    private fun setupConfirmButton() {
        binding.btnConfirmBooking.setOnClickListener {
            viewModel.confirmBooking()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: ConfirmBookingUiState) {
        // بيانات الدكتور
        state.doctor?.let { doctor ->
            binding.tvDoctorInitials.text = doctor.initials()
            binding.tvDoctorName.text = doctor.name
            binding.tvDoctorSpecialty.text = doctor.specialty
        }

        // السلوتات
        binding.progressBarSlots.visibility =
            if (state.isLoadingSlots) View.VISIBLE else View.GONE
        binding.rvSlots.visibility = if (state.isLoadingSlots) View.GONE else View.VISIBLE
        slotsAdapter.submitList(state.slots)

        // السلوت المختار
        if (state.selectedSlot != null && state.selectedDate != null) {
            val formatter = DateTimeFormatter.ofPattern("EEE MMM d")
            binding.tvSelectedSlot.text = getString(
                R.string.selected_slot_format,
                "${state.selectedDate.format(formatter)}, ${state.selectedSlot.time}"
            )
            binding.tvSelectedSlot.visibility = View.VISIBLE
        } else {
            binding.tvSelectedSlot.visibility = View.GONE
        }
        binding.btnConfirmBooking.isEnabled = state.selectedSlot != null && !state.isBooking

        // ============================================================
        // أهم نقطة في الشاشة دي (مطلوبة في الـ task الجديد):
        // لما الحجز شغال (isBooking = true)، نظهر الـ overlay اللي
        // بيغطي الشاشة كاملة، يمنع أي لمسة (clickable/focusable=true
        // في الـ XML)، ونوضح للمستخدم إن الداتا لسه بترفع على Firestore.
        // ده بيمنع الضغط المتكرر على "Confirm booking" أو الرجوع للخلف
        // قبل ما الرفع يخلص فعليًا.
        // ============================================================
        binding.layoutBookingOverlay.visibility =
            if (state.isBooking) View.VISIBLE else View.GONE

        // نجاح الحجز -> ننتقل لشاشة "Booked successfully"
        if (state.bookingSucceeded) {
            val action = ConfirmBookingFragmentDirections
                .actionConfirmBookingToBookedSuccessfully(state.lastBookedAppointmentId.orEmpty())
            findNavController().navigate(action)
            viewModel.onNavigatedAfterSuccess()
        }

        // فشل الحجز (مثلاً تعارض سلوت) -> نعرض رسالة بدون نقل الشاشة
        state.errorMessage?.let { message ->
            Snackbar
                .make(binding.root, message, Snackbar.LENGTH_LONG)
                .show()
            viewModel.onErrorMessageShown()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}