package com.example.docmate.presentation.main.booking.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.docmate.presentation.main.booking.adapter.BookingAdapter
import com.example.docmate.presentation.main.booking.viewmodel.BookingViewModel
import com.example.docmate.data.model.Appointment
import com.example.docmate.databinding.FragmentBookingBinding
import com.example.docmate.util.Resource
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookingFragment : Fragment() {

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookingViewModel by viewModels()
    private lateinit var adapter: BookingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupTabs()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let { viewModel.selectTab(it.position) }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = BookingAdapter(
            onBookingClick = { appointment ->
                // Placeholder for detail navigation
            }
        )
        binding.rvBookings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBookings.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnRetry.setOnClickListener {
            viewModel.loadAppointments()
        }
    }

    private fun observeViewModel() {
        viewModel.selectedTab.observe(viewLifecycleOwner) { tab ->
            val resource = viewModel.appointmentsState.value
            if (resource is Resource.Success) {
                applyFilter(resource.data, tab)
            } else if (resource is Resource.Empty) {
                showEmptyForTab(tab)
            }
        }

        viewModel.appointmentsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvBookings.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val tab = viewModel.selectedTab.value ?: 0
                    applyFilter(resource.data, tab)
                }
                is Resource.Empty -> {
                    binding.progressBar.visibility = View.GONE
                    val tab = viewModel.selectedTab.value ?: 0
                    showEmptyForTab(tab)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvBookings.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.tvEmptyTitle.text = "Something went wrong"
                    binding.tvEmptySubtitle.text = resource.exception.localizedMessage ?: "Please try again."
                }
            }
        }
    }

    private fun applyFilter(appointments: List<Appointment>, tab: Int) {
        val filtered = viewModel.getFilteredAppointments(appointments, tab)
        if (filtered.isEmpty()) {
            binding.rvBookings.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.tvEmptyTitle.text = getEmptyTitle(tab)
            binding.tvEmptySubtitle.text = getEmptySubtitle(tab)
        } else {
            binding.rvBookings.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
            adapter.submitList(filtered)
        }
    }

    private fun showEmptyForTab(tab: Int) {
        binding.rvBookings.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.tvEmptyTitle.text = getEmptyTitle(tab)
        binding.tvEmptySubtitle.text = getEmptySubtitle(tab)
    }

    private fun getEmptyTitle(tab: Int): String = when (tab) {
        1 -> "No Completed Appointments"
        2 -> "No Cancelled Appointments"
        else -> "No Upcoming Appointments"
    }

    private fun getEmptySubtitle(tab: Int): String = when (tab) {
        1 -> "Your completed appointments will appear here."
        2 -> "You have no cancelled appointments."
        else -> "You don\u2019t have any upcoming appointments."
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
