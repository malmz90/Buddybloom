package com.example.buddybloom.ui.game

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.R
import com.example.buddybloom.data.repository.AccountRepository
import com.example.buddybloom.databinding.FragmentProfileBinding
import com.example.buddybloom.ui.AboutInfoFragment
import com.example.buddybloom.ui.authentication.AccountViewModel
import com.example.buddybloom.ui.authentication.AccountViewModelFactory
import com.example.buddybloom.ui.authentication.AuthenticationActivity
import com.example.buddybloom.ui.authentication.DeleteAccountDialogFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var binding : FragmentProfileBinding
    private lateinit var avm: AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
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
        avm = ViewModelProvider(this, factory)[AccountViewModel::class.java]

        //Clicklistener for user to get popup about deleting their account
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

        //info popup about the app
        binding.ibInfo.setOnClickListener {
            val aboutInfoFragment = AboutInfoFragment()
            aboutInfoFragment.show(requireActivity().supportFragmentManager, "AboutInfoFragmentTag")
        }
        binding.ibSignout.setOnClickListener {
            avm.signOutUser { success ->
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

        // Need for Update Email
        avm.updateEmailStatus.observe(viewLifecycleOwner) { result ->
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

           when{
               newUserName.isNotEmpty() -> { avm.updateUserName(newUserName)
                   Toast.makeText(requireContext(),
                       "User name updated!",Toast.LENGTH_SHORT).show()
               }
               newMail.isNotEmpty() -> { avm.updateUserEmail(newMail) }
               else -> Toast.makeText(requireContext(),
                   " At least one field needs to be filled!",Toast.LENGTH_SHORT).show()
           }
        }
    }
}