package com.example.docmate.presentation.authentication.ui.forgetpassword

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
import com.example.docmate.databinding.FragmentForgetPasswordBinding
import com.example.docmate.presentation.authentication.viewmodel.AuthViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class ForgetPasswordFragment : Fragment() {
    private var _binding: FragmentForgetPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentForgetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        restoreForm()
        observeForm()
        observeState()

        binding.sendResetLinkButton.setOnClickListener { sendResetLink() }
        binding.signInLink.setOnClickListener {
            findNavController().navigate(R.id.action_forgetPasswordFragment_to_loginFragment)
        }
    }

    private fun restoreForm() {
        binding.emailInput.setText(viewModel.forgetPasswordEmail)
    }

    private fun observeForm() {
        binding.emailInput.doOnTextChanged { text, _, _, _ -> viewModel.forgetPasswordEmail = text?.toString().orEmpty() }
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
        }
    }

    private fun sendResetLink() {
        val email = binding.emailInput.value()

        if (email.isBlank()) {
            showMessage(getString(R.string.error_email_required))
            return
        }

        viewModel.sendResetLink(
            email,
            getString(R.string.reset_link_sent),
            getString(R.string.error_reset_failed)
        )
    }

    private fun setLoading(isLoading: Boolean) {
        binding.forgetPasswordProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.sendResetLinkButton.isEnabled = !isLoading
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