package com.example.docmate.presentation.main.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.docmate.R
import com.example.docmate.databinding.FragmentHomeBinding

import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint

/**
 * شاشة Home: تعرض قائمة الدكاترة، السيرش، وفلاتر التخصصات.
 *
 * ملاحظة: استخدام Firestore الفعلي بيكون جوه HomeViewModel/Repository
 * مش هنا - الـ Fragment مسؤوليته بس عرض الداتا والتفاعل مع اليوزر.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var doctorsAdapter: DoctorsAdapter
    private var locationRequested = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            fetchLocationAndSave()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        doctorsAdapter = DoctorsAdapter(
            onDoctorClick = { doctor ->
                val action = HomeFragmentDirections.actionHomeToDoctorDetail(doctor.id)
                findNavController().navigate(action)
            },
            onBookClick = { doctor ->
                val action = HomeFragmentDirections.actionHomeToDoctorDetail(doctor.id)
                findNavController().navigate(action)
            }
        )
        binding.rvDoctors.adapter = doctorsAdapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onSearchQueryChanged(s?.toString().orEmpty())
            }
        })
    }

    private fun setupSpecialtyChips(specialties: List<String>) {
        binding.chipGroupSpecialties.removeAllViews()

        val allChip = Chip(requireContext()).apply {
            text = getString(R.string.specialty_all)
            isCheckable = true
            isChecked = true
            id = View.generateViewId()
        }
        binding.chipGroupSpecialties.addView(allChip)

        specialties.forEach { specialty ->
            val chip = Chip(requireContext()).apply {
                text = specialty
                isCheckable = true
                id = View.generateViewId()
            }
            binding.chipGroupSpecialties.addView(chip)
        }

        binding.chipGroupSpecialties.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedChip = group.findViewById<Chip>(checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener)
            val specialty = checkedChip?.text?.toString().orEmpty()
            viewModel.onSpecialtyFilterChanged(
                if (specialty == getString(R.string.specialty_all)) null else specialty
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        renderState(state)
                    }
                }
                launch {
                    viewModel.specialties.collect { specialties ->
                        setupSpecialtyChips(specialties)
                    }
                }
            }
        }
    }

    private fun renderState(state: HomeUiState) {
        binding.progressBarHome.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.layoutEmptyState.visibility =
            if (!state.isLoading && state.doctors.isEmpty()) View.VISIBLE else View.GONE
        binding.rvDoctors.visibility = if (state.doctors.isNotEmpty()) View.VISIBLE else View.GONE

        doctorsAdapter.submitList(state.doctors)

        binding.tvUserName.text = state.userName

        if (state.userLocation.isNotEmpty() && state.userLocation != "Not specified") {
            binding.llLocation.visibility = View.VISIBLE
            binding.tvLocation.text = state.userLocation
        } else {
            binding.llLocation.visibility = View.GONE
            if (!state.isLoading && !locationRequested) {
                locationRequested = true
                checkLocationPermissionAndFetch()
            }
        }
    }

    private fun checkLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ))
            return
        }
        fetchLocationAndSave()
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocationAndSave() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                try {
                    val geocoder = Geocoder(requireContext(), java.util.Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val city = addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea
                        if (city != null) {
                            viewModel.saveLocation(city)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
