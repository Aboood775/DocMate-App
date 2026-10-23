package com.example.docmate.presentation.authentication.ui.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.docmate.R
import com.example.docmate.databinding.FragmentLoginBinding
import com.example.docmate.presentation.authentication.viewmodel.AuthViewModel
import com.example.docmate.presentation.authentication.activity.AuthActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()
    private var googleSignInClient: GoogleSignInClient? = null

    private val googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.signInWithGoogle(
                account.idToken,
                getString(R.string.error_google_sign_in_failed),
                getString(R.string.error_save_user_failed)

            )
        } catch (error: ApiException) {
            if (error.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                viewModel.clearLoading()
                return@registerForActivityResult
            }

            viewModel.signInWithGoogle(
                null,
                getString(R.string.error_google_sign_in_failed),
                getString(R.string.error_save_user_failed)
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGoogleClient()
        restoreForm()
        observeForm()
        observeState()

        binding.signInButton.setOnClickListener { signInWithEmail() }
        binding.googleSignInButton.setOnClickListener { signInWithGoogle() }
        binding.forgotPasswordLink.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgetPasswordFragment)
        }
        binding.registerLink.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun setupGoogleClient() {
        getDefaultWebClientId()?.let { webClientId ->
            googleSignInClient = GoogleSignIn.getClient(
                requireContext(),
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build()
            )
        }
    }

    private fun restoreForm() {
        binding.emailInput.setText(viewModel.loginEmail)
        binding.passwordInput.setText(viewModel.loginPassword)
    }

    private fun observeForm() {
        binding.emailInput.doOnTextChanged { text, _, _, _ -> viewModel.loginEmail = text?.toString().orEmpty() }
        binding.passwordInput.doOnTextChanged { text, _, _, _ -> viewModel.loginPassword = text?.toString().orEmpty() }
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

            if (state.shouldOpenMain) {
                viewModel.clearNavigation()
                (requireActivity() as AuthActivity).openMainAndFinish()
            }
        }
    }

    private fun signInWithEmail() {
        val email = binding.emailInput.value()
        val password = binding.passwordInput.value()

        when {
            email.isBlank() -> showMessage(getString(R.string.error_email_required))
            password.isBlank() -> showMessage(getString(R.string.error_password_required))
            else -> viewModel.signInWithEmail(
                email,
                password,
                getString(R.string.error_login_failed),
                "Please verify your email before signing in.",
                getString(R.string.error_save_user_failed)
            )
        }
    }

    private fun signInWithGoogle() {
        val client = googleSignInClient
        if (client == null) {
            showMessage(getString(R.string.error_google_client_missing))
            return
        }

        viewModel.setGoogleLoading()
        clearLoginFocusAndKeyboard()
        googleLauncher.launch(client.signInIntent)
    }

    private fun clearLoginFocusAndKeyboard() {
        binding.emailInput.clearFocus()
        binding.passwordInput.clearFocus()

        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun getDefaultWebClientId(): String? {
        val resourceId = resources.getIdentifier("default_web_client_id", "string", requireContext().packageName)
        if (resourceId == 0) return null
        return getString(resourceId).takeIf { it.isNotBlank() }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loginProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.signInButton.isEnabled = !isLoading
        binding.googleSignInButton.isEnabled = !isLoading
        binding.forgotPasswordLink.isEnabled = !isLoading
        binding.registerLink.isEnabled = !isLoading
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