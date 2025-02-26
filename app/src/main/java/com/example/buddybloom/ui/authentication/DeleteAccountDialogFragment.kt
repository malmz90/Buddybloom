package com.example.buddybloom.ui.authentication

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.R
import com.google.android.material.button.MaterialButton

// class for delete account dialog popup
class DeleteAccountDialogFragment : DialogFragment(R.layout.dialog_delete_account) {
    private lateinit var viewModel: AccountViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Creates a dialog box with custom design
        val builder = AlertDialog.Builder(requireContext())
        val inflater =  requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_delete_account, null)

        viewModel = ViewModelProvider(requireActivity())[AccountViewModel::class.java]

        builder.setView(view)

        //variables for buttons
        val btnNo : MaterialButton = view.findViewById(R.id.btn_no)
        val btnYes : MaterialButton = view.findViewById(R.id.btn_yes)

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
        viewModel.deleteAccount(
            onSuccess = {
                Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show()
                dismiss()
                activity?.finish()
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Failed to delete account: ${exception.message}", Toast.LENGTH_SHORT).show()
            })
    }
}