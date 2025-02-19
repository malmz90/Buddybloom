package com.example.buddybloom.ui.game

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.R
import com.example.buddybloom.databinding.FragmentProfileBinding
import com.example.buddybloom.ui.AboutInfoFragment
import com.example.buddybloom.ui.authentication.AccountViewModel
import com.example.buddybloom.ui.authentication.AuthenticationActivity
import com.example.buddybloom.ui.authentication.DeleteAccountDialogFragment

class ProfileFragment : Fragment() {

    private lateinit var binding : FragmentProfileBinding
    private lateinit var accountViewModel: AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountViewModel = ViewModelProvider(this)[AccountViewModel::class.java]

        binding.deleteAccountText.setOnClickListener {
            val deleteAccountDialog = DeleteAccountDialogFragment()
            deleteAccountDialog.show(parentFragmentManager, "DeleteAccountDialogFragment")
        }
        binding.dailyNotificationsSwitch.setOnCheckedChangeListener{ _,isChecked ->
            if(isChecked){
                Toast.makeText(requireContext(),"Notifications ON!!", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(requireContext(),"Notifications OFF!",Toast.LENGTH_SHORT).show()
            }
        }

        binding.ibInfo.setOnClickListener {
            val aboutInfoFragment = AboutInfoFragment()
            aboutInfoFragment.show(requireActivity().supportFragmentManager, "AboutInfoFragmentTag")
        }
        binding.btnSignOut.setOnClickListener {
            accountViewModel.signOutUser { success ->
                if(success){
                    Toast.makeText(requireContext(),"Signing out",Toast.LENGTH_SHORT).show()
                    val newIntent = Intent(requireContext(), AuthenticationActivity::class.java)
                    // NEW_TASK starts a new activity and CLEAR_TASK closes activity, so user can not go back without signing in again.
                    newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(newIntent)
                } else{
                    Toast.makeText(requireContext(),"Failed to signout!",Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnHistoryCheck.setOnClickListener {
            HistoryDialogFragment().show(parentFragmentManager, null)
        }
        accountViewModel.updateStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Update Success! Verfify before signing in!!.", Toast.LENGTH_LONG).show()

                //Navigates to Login page
                val intent = Intent(requireContext(), AuthenticationActivity::class.java)
                startActivity(intent)
                requireActivity().finish() // Shuts ProfileFragment, user can`t co back until signing in

            }.onFailure { exception ->
                Toast.makeText(requireContext(), "Fel: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
        binding.saveButton.setOnClickListener {
            val newMail = binding.etEmail2.text.toString()
            val newUserName = binding.etUsername.text.toString()
            accountViewModel.updateUser(newMail, newUserName)
        }
    }
}