package com.example.docmate.presentation.main.doctordetail.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.docmate.domain.model.Doctor
import com.example.docmate.domain.model.WorkingHoursRange
import com.example.docmate.databinding.FragmentScheduleTabBinding
import com.example.docmate.databinding.ItemWorkingHoursRowBinding
import com.example.docmate.presentation.main.doctordetail.DoctorDetailViewModel
import kotlinx.coroutines.launch

/**
 * تاب "Schedule" - بيعرض ساعات العمل الكاملة (بدون اختيار سلوت فعلي،
 * ده موجود في شاشة Confirm Booking المنفصلة بعد الضغط على
 * "Book appointment"). هنا بس preview سريع لساعات الدوام.
 */
class ScheduleTabFragment : Fragment() {

    private var _binding: FragmentScheduleTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DoctorDetailViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.doctor?.let { bindDoctor(it) }
                }
            }
        }
    }

    private fun bindDoctor(doctor: Doctor) {
        binding.layoutScheduleHours.removeAllViews()

        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        for (i in 0..6) {
            val range: WorkingHoursRange? = doctor.workingHours[i.toString()]
            val rowBinding = ItemWorkingHoursRowBinding.inflate(
                layoutInflater, binding.layoutScheduleHours, false
            )
            rowBinding.tvDayRange.text = dayNames[i]
            rowBinding.tvHoursRange.text =
                if (range != null) "${range.start} – ${range.end}" else "Closed"
            binding.layoutScheduleHours.addView(rowBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
