package com.example.dermabsa.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.example.dermabsa.R

class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnStart = view.findViewById<MaterialButton>(R.id.btn_start)

        btnStart.setOnClickListener {

            findNavController().navigate(R.id.action_welcome_to_home)

        }
    }
}