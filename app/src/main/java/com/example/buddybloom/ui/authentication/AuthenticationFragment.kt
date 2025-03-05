package com.example.buddybloom.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.ui.AboutInfoFragment
import com.example.buddybloom.R
import com.example.buddybloom.data.repository.AccountRepository
import com.example.buddybloom.databinding.FragmentAuthenticationBinding
import com.example.buddybloom.ui.game.GameActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class AuthenticationFragment : Fragment() {

    private lateinit var binding : FragmentAuthenticationBinding
    private lateinit var avm: AccountViewModel

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)

        avm.authenticateWithGoogle(task) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Google sign-in successful!", Toast.LENGTH_SHORT).show()
                navigateToGameActivity()
            } else {
                Toast.makeText(requireContext(), "Google sign-in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthenticationBinding.inflate(inflater,container, false)
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


        val activity = requireActivity() as? AuthenticationActivity
        activity?.binding?.btnBack?.visibility = View.GONE

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

        binding.btnGoogle.setOnClickListener {
            val signInIntent = avm.getGoogleSignInIntent()
            googleSignInLauncher.launch(signInIntent)
        }

        //opens info about the app when pressing info button
        binding.btnAbout.setOnClickListener {
            val aboutInfoFragment = AboutInfoFragment()
            aboutInfoFragment.show(requireActivity().supportFragmentManager, "AboutInfoFragmentTag")
        }
    }

    private fun navigateToGameActivity() {
        activity?.let {
            val intent = Intent(it, GameActivity::class.java)
            startActivity(intent)
            it.finish()
        }
    }
}

//    private fun checkLoggedInUser(googleSignInClient: GoogleSignInClient) {
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        Log.d("AuthFragment", "Checking Google login, current user: ${currentUser?.email}")
//        if(currentUser != null) {
//            showLoginChoiceDialog(currentUser.email ?: "Unknown", googleSignInClient)
//        } else {
//            val signInIntent = googleSignInClient.signInIntent
//            googleSignInLauncher.launch(signInIntent)
//        }
//    }

//    private fun showLoginChoiceDialog(email:String, googleSignInClient: GoogleSignInClient) {
//        val builder = AlertDialog.Builder(requireContext())
//        builder.setTitle("Already Logged In")
//        builder.setMessage("You are currently logged in as $email.\nWould you like to continue or sign in with different account?")
//        builder.setPositiveButton("Continue") {_, _ ->
//            Toast.makeText(requireContext(), "Logging in as $email", Toast.LENGTH_SHORT).show()
//            navigateToGameActivity()
//        }
//        builder.setNegativeButton("Different account") { _, _ ->
//            avm.signOutUser { success->
//                if(success) {
//                    Toast.makeText(requireContext(), "Successfully signed out", Toast.LENGTH_SHORT).show()
//
//
//                    val signInIntent = googleSignInClient.signInIntent
//                    googleSignInLauncher.launch(signInIntent)
//                } else {
//                    Toast.makeText(requireContext(), "Failed to sign out", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//        builder.setCancelable(false)
//        val dialog = builder.create()
//        dialog.show()
//    }