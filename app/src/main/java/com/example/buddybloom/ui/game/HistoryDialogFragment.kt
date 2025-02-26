package com.example.buddybloom.ui.game

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.buddybloom.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class HistoryDialogFragment : DialogFragment(R.layout.dialog_history) {
    private lateinit var placeHolderTextView: MaterialTextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var hvm: HistoryViewModel
    private lateinit var closeButton: MaterialButton

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hvm = ViewModelProvider(this)[HistoryViewModel::class.java]
        placeHolderTextView = view.findViewById(R.id.tv_empty_history)
        closeButton = view.findViewById(R.id.btn_history_close)
        recyclerView = view.findViewById(R.id.rv_history)

        val adapter = HistoryAdapter(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Observe history items
        hvm.historyItems.observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) {
                recyclerView.visibility = View.GONE
                placeHolderTextView.visibility = View.VISIBLE
            } else {
                placeHolderTextView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.update(items.toMutableList())
            }
        }

        hvm.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        closeButton.setOnClickListener { dismiss() }

        hvm.loadHistory()
    }
}