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


        binding.btnRegister.setOnClickListener {
            registerUser()
        }


        avm.registerResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Registration succeeded!", Toast.LENGTH_SHORT).show()
                removeFragment()
            } else {
                Toast.makeText(context, "Registration failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }




    private fun registerUser() {
        val email = binding.tietEmail.text.toString()
        val password = binding.tietPassword.text.toString()
        val name = binding.tietUsername.text.toString()

        avm.registerUser(email, password, name)
    }

    private fun removeFragment() {
        parentFragmentManager.beginTransaction().apply{
            remove(this@RegisterFragment)
            commit()
        }
    }

}

