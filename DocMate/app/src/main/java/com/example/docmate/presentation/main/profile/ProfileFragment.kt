package com.example.docmate.presentation.main.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.docmate.R
import com.example.docmate.databinding.FragmentProfileBinding
import com.example.docmate.presentation.authentication.activity.AuthActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Observe Profile Data to update XML bindings
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            binding.profile = profile
        }
        binding.btnPaymentMethods.setOnClickListener {
            Snackbar.make(requireView(), "Cash when arrived", Snackbar.LENGTH_LONG).show()
        }
        binding.btnSupport.setOnClickListener {
            Snackbar.make(requireView(), "Support is not available yet", Snackbar.LENGTH_LONG).show()
        }


        viewModel.isLoggedOut.observe(viewLifecycleOwner) { loggedOut ->
            if (loggedOut) {
                val intent = Intent(requireContext(), AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                requireActivity().finish()
            }
        }

        setupMenuClicks()
    }

    private fun setupMenuClicks() {
        binding.btnPersonalInfo.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_personalInfo)
        }
        
        binding.btnPaymentMethods.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_paymentMethods)
        }

        binding.btnAppointments.setOnClickListener {
            findNavController().navigate(R.id.appointmentFragment)
        }

        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        binding.switchDarkMode.isChecked = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}
