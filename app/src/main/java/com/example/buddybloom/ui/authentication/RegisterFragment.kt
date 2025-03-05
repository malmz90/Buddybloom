package com.example.buddybloom.ui.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.R
import com.example.buddybloom.data.repository.AccountRepository
import com.example.buddybloom.databinding.FragmentRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    lateinit var binding: FragmentRegisterBinding
    private lateinit var avm: AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        val accountRepository = AccountRepository(FirebaseAuth.getInstance(), googleSignInClient)
        val factory = AccountViewModelFactory(accountRepository)
        avm = ViewModelProvider(requireActivity(), factory)[AccountViewModel::class.java]

        avm.errorMessage.observe(viewLifecycleOwner) {
            it?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        //sets back button visible when register new user
        val activity = requireActivity() as? AuthenticationActivity
        activity?.binding?.btnBack?.visibility = View.VISIBLE

        /**
         * Calls upon function in AccountRepo to register user (through the viewmodel)
         */
        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        /**
         * Observes the registration result livedata in the viewmodel provided by function in AccountRepo.
         * If result is true, the user is navigated to the login fragment.
         */
        avm.registerResult.observe(viewLifecycleOwner) { success ->
            binding.progressBar.visibility = View.GONE
            if (success) {
                removeFragment()
            }
        }
    }

    private fun isPasswordStrong(password: String): Boolean {
        val passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@!#\$%^&+=]).{8,}$"
        return password.matches(passwordPattern.toRegex())
    }

    /**
     * Calls upon function in AccountRepo through the viewmodel to register user
     * Takes in user's input as parameters.
     */
    private fun registerUser() {
        val email = binding.tietEmail.text.toString().trim()
        val password = binding.tietPassword.text.toString().trim()
        val confirmPassword = binding.tietConfirmpassword.text.toString().trim()
        val name = binding.tietUsername.text.toString().trim()

        // Check that the fields are not empty
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
            Toast.makeText(context, getString(R.string.fill_in_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        // Check the email is in the correct format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, getString(R.string.invalid_email_address), Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the password meets security
        if (!isPasswordStrong(password)) {
            Toast.makeText(
                context, getString(R.string.password_characters) +
                        getString(R.string.password_contains),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Check that the passwords match
        if (password != confirmPassword) {
            Toast.makeText(context,
                getString(R.string.the_passwords_do_not_match), Toast.LENGTH_SHORT).show()
            return
        }
        avm.registerUser(email, password, name)
    }

    private fun removeFragment() {
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fcv_home, AuthenticationFragment())
            commit()
        }
    }
}