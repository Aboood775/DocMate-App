package com.example.docmate.presentation.main.confirmbooking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.docmate.R
import com.example.docmate.databinding.FragmentBookedSuccessfullyBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * شاشة "All confirmed!" بعد نجاح الحجز.
 * تقوم بجلب تفاصيل الحجز الفعلي من Firestore وعرضها للمستخدم في بطاقة ملخص الحجز.
 */
class BookedSuccessfullyFragment : Fragment() {

    private var _binding: FragmentBookedSuccessfullyBinding? = null
    private val binding get() = _binding!!

    private val args: BookedSuccessfullyFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookedSuccessfullyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        loadAppointmentDetails()
    }

    private fun setupButtons() {
        binding.btnAddToCalendar.setOnClickListener {
            Toast.makeText(requireContext(), "Added to calendar successfully", Toast.LENGTH_SHORT).show()
        }

        binding.btnViewDetails.setOnClickListener {
            // الانتقال لشاشة الحجوزات (Appts) وتصفير الـ stack إلى HomeFragment
            findNavController().navigate(
                R.id.appointmentFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, false)
                    .build()
            )
        }
    }

    private fun loadAppointmentDetails() {
        val appointmentId = args.appointmentId
        if (appointmentId.isEmpty()) return

        FirebaseFirestore.getInstance().collection("appointments").document(appointmentId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists() && isAdded) {
                    val doctorName = document.getString("doctorName").orEmpty()
                    val doctorSpecialty = document.getString("doctorSpecialty").orEmpty()
                    val doctorInitials = document.getString("doctorInitials").orEmpty()
                    val location = document.getString("location").orEmpty()
                    val rawDate = document.getString("date").orEmpty()
                    val time = document.getString("time").orEmpty()

                    // عرض الأحرف الأولى
                    binding.tvDoctorInitials.text = doctorInitials
                    binding.tvDoctorName.text = doctorName
                    binding.tvDoctorSpecialty.text = doctorSpecialty
                    binding.tvAppointmentLocation.text = location
                    binding.tvAppointmentTime.text = time

                    // تنسيق التاريخ ليكون مثل Sat, May 2
                    try {
                        val date = LocalDate.parse(rawDate)
                        val formattedDate = date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
                        binding.tvAppointmentDate.text = formattedDate
                    } catch (e: Exception) {
                        binding.tvAppointmentDate.text = rawDate
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
