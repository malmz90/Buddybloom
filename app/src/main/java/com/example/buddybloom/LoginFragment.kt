package com.example.buddybloom

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
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout

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

        avm = ViewModelProvider(this)[AccountViewModel::class.java]

        view.setOnTouchListener { _, _ ->
            (activity as? HomeActivity)?.hideKeyboard()
            false
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
                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                navigateToGameActivity()
            } else {
                Toast.makeText(context, "Login failed! Check your fields", Toast.LENGTH_SHORT).show()
            }
        }
        avm.resetPasswordResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Check your Email!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Invalid Email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPass.text.toString().trim()

        binding.textinputPwLayout.error = null
        binding.textinputEtLayout.error = null

        var isValid = true

        if (email.isEmpty()) {
            binding.textinputEtLayout.error = "Email is required!"
            isValid = false
        }
        if (password.isEmpty()) {
            binding.textinputPwLayout.error = "Password is required!"
            isValid = false
        }
        if (!isValid) return

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.textinputEtLayout.error = "Please enter a valid Email address"
            return
        }
        if(password.length < 8) {
            binding.textinputPwLayout.error = "The password must be at least 8 characters long " +
                    "and contain an uppercase letter, lowercase letter, number and special character!"
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

    private fun forgotPassword() {
        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_forgot, null)
        val userEmailLayout = view.findViewById<TextInputLayout>(R.id.et_email_forgot)
        val userEmail = userEmailLayout.editText

        builder.setView(view)
        val dialog = builder.create()

        view.findViewById<Button>(R.id.btn_reset).setOnClickListener {
            val email = userEmail?.text.toString().trim()

            if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Enter a valid Email", Toast.LENGTH_SHORT).show()
            }else {
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