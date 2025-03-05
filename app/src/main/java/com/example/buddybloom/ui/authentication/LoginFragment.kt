package com.example.buddybloom.ui.authentication

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.databinding.FragmentLoginBinding
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.example.buddybloom.ui.game.GameActivity
import com.example.buddybloom.R
import com.example.buddybloom.data.repository.AccountRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var avm: AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
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

        //shows back button on loginpage
        val activity = requireActivity() as? AuthenticationActivity
        activity?.binding?.btnBack?.visibility = View.VISIBLE

        //hides keyboard
        view.setOnClickListener {
            activity?.hideKeyboard()
        }

        binding.tvForgot.setOnClickListener {
            forgotPassword()
        }
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        avm.loginResult.observe(viewLifecycleOwner) { success ->
            binding.progressBar.visibility = View.GONE
            if (success) {
                Toast.makeText(context, getString(R.string.login_successful), Toast.LENGTH_SHORT).show()
                navigateToGameActivity()
            } else {
                Toast.makeText(context,
                    getString(R.string.login_failed_check_your_fields), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        avm.loginStatus.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Toast.makeText(
                    requireContext(),
                    "Logged in as ${user.displayName}",
                    Toast.LENGTH_SHORT
                ).show()
                navigateToGameActivity()
            } else {
                Toast.makeText(requireContext(),
                    getString(R.string.failed_to_log_in_with_google), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        avm.resetPasswordResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(),
                    getString(R.string.check_your_email), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(),
                    getString(R.string.invalid_email), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPass.text.toString().trim()

        //shows built in error message design if invalid fields
        binding.textinputPwLayout.error = null
        binding.textinputEtLayout.error = null

        var isValid = true

        if (email.isEmpty()) {
            binding.textinputEtLayout.error = getString(R.string.email_is_required)
            isValid = false
        }
        if (password.isEmpty()) {
            binding.textinputPwLayout.error = getString(R.string.password_is_required)
            isValid = false
        }
        if (!isValid) return

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textinputEtLayout.error = getString(R.string.enter_a_valid_email)
            return
        }
        if (password.length < 8) {
            binding.textinputPwLayout.error = getString(R.string.password_contains)
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        avm.loginUser(email, password)
    }

    private fun navigateToGameActivity() {
        activity?.let {
            val intent = Intent(it, GameActivity::class.java)
            startActivity(intent)
            it.finish()
        }
    }

    //opens dialog box if user press Forgot Password
    private fun forgotPassword() {
        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_forgot, null)
        val userEmailLayout = view.findViewById<TextInputLayout>(R.id.et_email_forgot)
        val userEmail = userEmailLayout.editText

        builder.setView(view)
        val dialog = builder.create()

        view.findViewById<Button>(R.id.btn_reset).setOnClickListener {
            val email = userEmail?.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(),
                    getString(R.string.enter_a_valid_email), Toast.LENGTH_SHORT).show()
            } else {
                avm.sendPasswordResetEmail(email)
                dialog.dismiss()
            }
        }
        view.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
}