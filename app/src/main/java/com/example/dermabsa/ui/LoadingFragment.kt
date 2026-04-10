package com.example.dermabsa.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dermabsa.R
import com.example.dermabsa.utils.AILesionDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoadingFragment : Fragment(R.layout.fragment_loading) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val safeContext = requireContext()
        // Lanciamo l'analisi in un thread separato (Dispatchers.Default) per non bloccare l'interfaccia
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            try {
                val bitmap = viewModel.patientPhoto.value
                    ?: throw IllegalStateException("Immagine non trovata.")
                val region = viewModel.selectedRegion.value
                    ?: throw IllegalStateException("Distretto anatomico non selezionato.")

                val regionTotalPixels = bitmap.width * bitmap.height

                // 2. Usiamo il safeContext al posto di requireContext()
                val aiDetector = AILesionDetector(requireContext())
                val (result, highlightedBitmap) = aiDetector.analyzeImageAndCalculateBsa(bitmap, region, regionTotalPixels)
                aiDetector.close()

                // 3. Torniamo sul Main Thread per aggiornare la UI e navigare
                withContext(Dispatchers.Main) {
                    viewModel.finalBsaResult.value = result
                    viewModel.patientPhoto.value = highlightedBitmap

                    findNavController().navigate(R.id.action_loading_to_result)
                }

            } catch (e: Exception) {
                // In caso di errore (es. modello TFLite mancante o foto nulla), torniamo indietro in sicurezza
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Errore durante l'analisi: ${e.message}", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
            }
        }
    }
}