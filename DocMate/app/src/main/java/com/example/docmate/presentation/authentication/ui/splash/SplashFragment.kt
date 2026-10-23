package com.example.docmate.presentation.authentication.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.docmate.R
import com.example.docmate.databinding.FragmentSplashBinding
import com.example.docmate.presentation.authentication.viewmodel.AuthViewModel
import com.example.docmate.presentation.authentication.activity.AuthActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint


class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.isLoggedIn()) {
            (requireActivity() as AuthActivity).openMainAndFinish()
            return
        }

        binding.getStartedButton.setOnClickListener {
            findNavController().navigate(R.id.action_splashFragment_to_registerFragment)
        }

        binding.alreadyHaveAccountButton.setOnClickListener {
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}