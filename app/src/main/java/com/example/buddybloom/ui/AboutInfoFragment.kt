package com.example.buddybloom.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.buddybloom.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

//About the app info bottom sheet dialog
class AboutInfoFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about_info, container, false)
    }
}