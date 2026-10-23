package com.example.docmate.presentation.authentication.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.docmate.R
import com.example.docmate.databinding.FragmentRegisterBinding
import com.example.docmate.presentation.authentication.viewmodel.AuthViewModel
import com.example.docmate.presentation.authentication.activity.AuthActivity
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        restoreForm()
        observeForm()
        observeState()

        binding.createAccountButton.setOnClickListener { createAccount() }
        binding.signInLink.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun restoreForm() {
        binding.fullNameInput.setText(viewModel.registerName)
        binding.emailInput.setText(viewModel.registerEmail)
        binding.passwordInput.setText(viewModel.registerPassword)
    }

    private fun observeForm() {
        binding.fullNameInput.doOnTextChanged { text, _, _, _ -> viewModel.registerName = text?.toString().orEmpty() }
        binding.emailInput.doOnTextChanged { text, _, _, _ -> viewModel.registerEmail = text?.toString().orEmpty() }
        binding.passwordInput.doOnTextChanged { text, _, _, _ -> viewModel.registerPassword = text?.toString().orEmpty() }
    }

    private fun observeState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            setLoading(state.isLoading)

            state.successMessage?.let {
                showMessage(it)
                viewModel.clearMessage()
            }

            state.errorMessage?.let {
                showMessage(it)
                viewModel.clearMessage()
            }

            if (state.shouldOpenLogin) {
                viewModel.clearNavigation()
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }

            if (state.shouldOpenMain) {
                viewModel.clearNavigation()
                (requireActivity() as AuthActivity).openMainAndFinish()
            }
        }
    }

    private fun createAccount() {
        val fullName = binding.fullNameInput.value()
        val email = binding.emailInput.value()
        val password = binding.passwordInput.value()

        when {
            fullName.isBlank() -> showMessage(getString(R.string.error_full_name_required))
            email.isBlank() -> showMessage(getString(R.string.error_email_required))
            password.length < 6 -> showMessage(getString(R.string.error_password_too_short))
            else -> viewModel.createAccount(
                fullName,
                email,
                password,
                "Verification email sent. Please verify your email, then sign in.",
                getString(R.string.error_register_failed),
                getString(R.string.error_save_user_failed)
            )
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.registerProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.createAccountButton.isEnabled = !isLoading
        binding.signInLink.isEnabled = !isLoading
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun TextInputEditText.value(): String = text?.toString()?.trim().orEmpty()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}