package com.example.buddybloom

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.databinding.FragmentLoginBinding

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

        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.tvForgot.setOnClickListener {
            // Handle forgot password - you can implement this later
        }

        avm.loginResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                navigateToGameActivity()
            } else {
                Toast.makeText(context, "Login failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPass.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        avm.loginUser(email, password)
    }

    private fun navigateToGameActivity() {
        activity?.let {
            val intent = Intent(it, GameActivity::class.java)
            startActivity(intent)
            it.finish() // This will close the current activity containing the login fragment
        }
    }
}