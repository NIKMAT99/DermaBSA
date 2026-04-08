package com.example.dermabsa.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.example.dermabsa.R

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnStartScan = view.findViewById<MaterialButton>(R.id.btn_start)

        btnStartScan.setOnClickListener {

            findNavController().navigate(R.id.action_home_to_workspace)

        }
    }
}