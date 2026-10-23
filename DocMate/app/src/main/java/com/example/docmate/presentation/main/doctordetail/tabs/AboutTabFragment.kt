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
import com.example.docmate.databinding.FragmentAboutTabBinding
import com.example.docmate.databinding.ItemWorkingHoursRowBinding
import com.example.docmate.domain.model.WorkingHoursRange
import com.example.docmate.presentation.main.doctordetail.DoctorDetailViewModel

import kotlinx.coroutines.launch

/**
 * تاب "About" جوه شاشة Doctor Detail.
 *
 * ملاحظة مهمة عن الطريقة المستخدمة: بدل ما نمرر بيانات الدكتور كـ
 * Bundle/Parcelable بين الـ Fragment الأب والتابات، التاب ده بيشارك
 * *نفس* الـ DoctorDetailViewModel بتاع DoctorDetailFragment الأب
 * (عن طريق requireParentFragment()). ده أنضف لأن:
 * 1. الداتا مصدرها مكان واحد بس (مفيش تكرار/نسخ بيانات)
 * 2. لو الدكتور تحدث (مثلاً بعد تحديث من Firestore)، كل التابات
 *    تتحدث تلقائيًا من غير أي كود إضافي.
 */
class AboutTabFragment : Fragment() {

    private var _binding: FragmentAboutTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DoctorDetailViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutTabBinding.inflate(inflater, container, false)
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
        binding.tvAboutDescription.text = doctor.about

        binding.layoutWorkingHours.removeAllViews()

        // نجمع الأيام المتشابهة في ساعات العمل في صف واحد (زي Mon-Fri)
        // بدل ما نعرض كل يوم في صف منفصل، حسب شكل التصميم في فيجما.
        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val groupedRanges = groupConsecutiveDays(doctor.workingHours, dayNames)

        groupedRanges.forEach { (label, hoursLabel) ->
            val rowBinding = ItemWorkingHoursRowBinding.inflate(
                layoutInflater, binding.layoutWorkingHours, false
            )
            rowBinding.tvDayRange.text = label
            rowBinding.tvHoursRange.text = hoursLabel
            binding.layoutWorkingHours.addView(rowBinding.root)
        }
    }

    /**
     * بيجمع الأيام المتتالية بنفس ساعات العمل في نص واحد، مثلاً
     * "Mon - Fri" بدل ما نكرر "09:00-17:00" خمس مرات منفصلة.
     */
    private fun groupConsecutiveDays(
        workingHours: Map<String, WorkingHoursRange?>,
        dayNames: List<String>
    ): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        var i = 0
        while (i < 7) {
            val range = workingHours[i.toString()]
            if (range == null) {
                i++
                continue
            }
            var j = i
            while (j + 1 < 7 && workingHours[(j + 1).toString()] == range) {
                j++
            }
            val label = if (i == j) dayNames[i] else "${dayNames[i]} - ${dayNames[j]}"
            result.add(label to "${range.start} – ${range.end}")
            i = j + 1
        }
        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
