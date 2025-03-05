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

        avm.loadUserData()

        //Sets current user data in email och username fields
        avm.currentUserData.observe(viewLifecycleOwner) { user ->
            if (avm.isSigningOut.value != true) {
                user?.let {
                    binding.etEmail2.setText(it.email ?: "")
                    binding.etUsername.setText(it.name ?: "")

                    binding.tvHello.text =
                        "BuddyBloom Master, \n ${it.name ?: "User"}! \uD83D\uDC4B"
                } ?: run {
                    binding.etEmail2.setText("")
                    binding.etUsername.setText("")
                    binding.tvHello.text = getString(R.string.buddybloom)
                }
            }
        }

        //Clicklistener for user to get popup about deleting their account
        binding.deleteAccountText.setOnClickListener {
            val deleteAccountDialog = DeleteAccountDialogFragment()
            deleteAccountDialog.show(parentFragmentManager, "DeleteAccountDialogFragment")
        }
        binding.dailyNotificationsSwitch.setOnCheckedChangeListener{ _,isChecked ->
            if(isChecked){
                Toast.makeText(requireContext(),
                    getString(R.string.notifications_on), Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(requireContext(),
                    getString(R.string.notifications_off),Toast.LENGTH_SHORT).show()
            }
        }

        //info popup about the app
        binding.ibInfo.setOnClickListener {
            val aboutInfoFragment = AboutInfoFragment()
            aboutInfoFragment.show(requireActivity().supportFragmentManager, "AboutInfoFragmentTag")
        }

        binding.ibSignout.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            avm.signOutUser()
            Toast.makeText(requireContext(), getString(R.string.signing_out),Toast.LENGTH_SHORT).show()
                    val newIntent = Intent(requireContext(), AuthenticationActivity::class.java)
                    // NEW_TASK starts a new activity and CLEAR_TASK closes activity, so user can not go back without signing in again.
                    newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(newIntent)
                binding.progressBar.visibility = View.GONE
        }

        binding.btnHistoryCheck.setOnClickListener {
            HistoryDialogFragment().show(parentFragmentManager, null)
        }

        // Need for Update Email
        avm.updateEmailStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(),
                    getString(R.string.update_success_verify_before_signing_in), Toast.LENGTH_LONG).show()

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
                       getString(R.string.user_name_updated),Toast.LENGTH_SHORT).show()
               }
               newMail.isNotEmpty() -> { avm.updateUserEmail(newMail) }
               else -> Toast.makeText(requireContext(),
                   getString(R.string.at_least_one_field_needs_to_be_filled), Toast.LENGTH_SHORT).show()
           }
        }
    }
}