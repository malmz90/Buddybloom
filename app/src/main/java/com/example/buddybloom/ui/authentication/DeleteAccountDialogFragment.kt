package com.example.buddybloom.ui.authentication

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.R
import com.example.buddybloom.data.repository.AccountRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

// class for delete account dialog popup
class DeleteAccountDialogFragment : DialogFragment(R.layout.dialog_delete_account) {
    private lateinit var avm: AccountViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Creates a dialog box with custom design
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_delete_account, null)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        val accountRepository = AccountRepository(FirebaseAuth.getInstance(), googleSignInClient)
        val factory = AccountViewModelFactory(accountRepository)
        avm = ViewModelProvider(requireActivity(), factory)[AccountViewModel::class.java]

        builder.setView(view)

        //variables for buttons
        val btnNo: MaterialButton = view.findViewById(R.id.btn_no)
        val btnYes: MaterialButton = view.findViewById(R.id.btn_yes)

        btnNo.setOnClickListener {
            dismiss()
        }
        btnYes.setOnClickListener {
            deleteAccount()
        }

        val dialog = builder.create()
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    //gets function from viewmodel to delete account and exits the app if deleted
    private fun deleteAccount() {
        avm.deleteAccount(onSuccess = {
            Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
            dismiss()
        })
    }
}