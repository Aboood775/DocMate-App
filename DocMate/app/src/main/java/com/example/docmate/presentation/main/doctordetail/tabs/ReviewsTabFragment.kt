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
import com.example.docmate.databinding.FragmentReviewsTabBinding
import com.example.docmate.presentation.main.doctordetail.DoctorDetailViewModel
import kotlinx.coroutines.launch

class ReviewsTabFragment : Fragment() {

    private var _binding: FragmentReviewsTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DoctorDetailViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewsTabBinding.inflate(inflater, container, false)
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
        binding.tvReviewsSummary.text = String.format(
            "%.1f ★ · %d reviews", doctor.rating, doctor.reviewCount
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
