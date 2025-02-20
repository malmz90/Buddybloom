package com.example.buddybloom.ui.authentication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.buddybloom.ui.AboutInfoFragment
import com.example.buddybloom.R
import com.example.buddybloom.databinding.FragmentAuthenticationBinding

class AuthenticationFragment : Fragment() {

    private lateinit var binding : FragmentAuthenticationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthenticationBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as? AuthenticationActivity
        activity?.binding?.btnBack?.visibility = View.GONE

        //TODO behövs dessa?
        val registerFragment = RegisterFragment()
        val loginFragment = LoginFragment()

        binding.btnRegister.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fcv_home, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnSignIn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fcv_home, LoginFragment())
                .addToBackStack(null)
                .commit()
        }

        //opens info about the app when pressing info button
        binding.btnAbout.setOnClickListener {
            val aboutInfoFragment = AboutInfoFragment()
            aboutInfoFragment.show(requireActivity().supportFragmentManager, "AboutInfoFragmentTag")
        }
    }
}