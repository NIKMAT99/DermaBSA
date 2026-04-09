package com.example.dermabsa.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dermabsa.R
import com.google.android.material.button.MaterialButton
import java.util.Locale

class ResultFragment : Fragment(R.layout.fragment_result) {

    // Recupera il ViewModel condiviso per leggere il risultato e l'immagine
    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Associazione degli ID presenti nel tuo fragment_result.xml
        val tvBsaResult = view.findViewById<TextView>(R.id.tv_bsa_result)
        val btnExitHome = view.findViewById<MaterialButton>(R.id.btn_exit_home)
        val btnNewScan = view.findViewById<MaterialButton>(R.id.btn_new_scan)
        val ivPreviewPhoto = view.findViewById<ImageView>(R.id.iv_preview_photo)

        // 1. Mostra la foto del paziente (se presente)
        viewModel.patientPhoto.value?.let { bitmap ->
            ivPreviewPhoto.setImageBitmap(bitmap)
        }

        // 2. Legge il risultato dall'IA e aggiorna il testo
        viewModel.finalBsaResult.value?.let { result ->
            val formattedPercentage = String.format(Locale.getDefault(), "%.2f", result.finalInvolvedPercentage)
            tvBsaResult.text = "Calcolo BSA: $formattedPercentage %"
        }

        // 3. Azioni di navigazione
        btnExitHome.setOnClickListener {
            findNavController().navigate(R.id.action_result_to_home)
        }

        btnNewScan.setOnClickListener {
            findNavController().navigate(R.id.action_result_to_workspace)
        }
    }
}