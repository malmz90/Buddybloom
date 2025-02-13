package com.example.buddybloom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.databinding.FragmentRegisterBinding
import com.google.android.material.textfield.TextInputEditText

class RegisterFragment : Fragment() {

    lateinit var binding : FragmentRegisterBinding
    private lateinit var avm : AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        avm = ViewModelProvider(this)[AccountViewModel::class.java]

        view.setOnTouchListener { _, _ ->
            (activity as? HomeActivity)?.hideKeyboard()
            false
        }

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        avm.registerResult.observe(viewLifecycleOwner) { success ->
            binding.progressBar.visibility = View.GONE
            if (success) {
                Toast.makeText(context, "Registration succeeded!", Toast.LENGTH_SHORT).show()
                removeFragment()
            } else {
                Toast.makeText(context, "Registration failed! Check all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to check if the password is strong
    private fun isPasswordStrong(password: String): Boolean {
//        val passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@!#\$%^&+=]).{8,}$"
        val passwordPattern = "^(?=.*[a-z]).{3,}$" // Tempo under dev
        return password.matches(passwordPattern.toRegex())
    }

    private fun registerUser() {
        val email = binding.tietEmail.text.toString().trim()
        val password = binding.tietPassword.text.toString().trim()
        val confirmPassword = binding.tietConfirmpassword.text.toString().trim()
        val name = binding.tietUsername.text.toString().trim()

        // Check that the fields are not empty
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()|| name.isEmpty()) {
            Toast.makeText(context, "Fill in all fields!", Toast.LENGTH_SHORT).show()
            return
        }

        // Check the email is in the correct format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Invalid email address!", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the password meets security
        if (!isPasswordStrong(password)) {
            Toast.makeText(context, "The password must be at least 8 characters long " +
                    "and contain an uppercase letter, lowercase letter, number and special character!",
                Toast.LENGTH_LONG).show()
            return
        }

        // Check that the passwords match
        if (password != confirmPassword) {
            Toast.makeText(context, "The passwords do not match!", Toast.LENGTH_SHORT).show()
            return
        }

        avm.registerUser(email, password, name)
    }

    private fun removeFragment() {
        parentFragmentManager.beginTransaction().apply{
            remove(this@RegisterFragment)
            commit()
        }
    }
}