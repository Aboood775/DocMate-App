package com.example.docmate.presentation.main.doctordetail

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
import com.example.docmate.databinding.FragmentDoctorDetailBinding

import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class DoctorDetailFragment : Fragment() {

    private var _binding: FragmentDoctorDetailBinding? = null
    private val binding get() = _binding!!

    private val args: DoctorDetailFragmentArgs by navArgs()
    private val viewModel: DoctorDetailViewModel by viewModels {
        DoctorDetailViewModelFactory(args.doctorId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnBookAppointment.setOnClickListener {
            val doctor = viewModel.uiState.value.doctor ?: return@setOnClickListener
            val action = DoctorDetailFragmentDirections
                .actionDoctorDetailToConfirmBooking(doctor.id)
            findNavController().navigate(action)
        }

        setupTabs()

        observeViewModel()
    }

    /**
     * بتربط ViewPager2 بالتابات الثلاثة (About/Schedule/Reviews) وتوصله
     * بالـ TabLayout الموجود في الـ XML عن طريق TabLayoutMediator.
     * أسماء التابات هنا لازم تتطابق بالترتيب مع DoctorDetailPagerAdapter
     * (position 0 = About, 1 = Schedule, 2 = Reviews).
     */
    private fun setupTabs() {
        binding.viewPagerDoctorDetail.adapter =
            DoctorDetailPagerAdapter(childFragmentManager, lifecycle)

        val tabTitles = listOf(
            getString(R.string.tab_about),
            getString(R.string.tab_schedule),
            getString(R.string.tab_reviews)
        )

        TabLayoutMediator(
            binding.tabLayoutDoctorDetail,
            binding.viewPagerDoctorDetail
        ) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val doctor = state.doctor ?: return@collect
                    binding.tvDoctorInitials.text = doctor.initials()
                    binding.tvDoctorName.text = doctor.name
                    binding.tvDoctorSpecialty.text = doctor.specialty
                    binding.tvDoctorRating.text = String.format("%.1f", doctor.rating)
                    binding.tvReviewCount.text = "(${doctor.reviewCount})"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
