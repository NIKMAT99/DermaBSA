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

        // Lanciamo l'analisi in un thread separato (Dispatchers.Default) per non bloccare l'interfaccia
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            try {
                // 1. Recupero dei dati reali salvati nei frammenti precedenti
                val bitmap = viewModel.patientPhoto.value
                    ?: throw IllegalStateException("Immagine non trovata.")
                val region = viewModel.selectedRegion.value
                    ?: throw IllegalStateException("Distretto anatomico non selezionato.")

                // Per i pixel totali della regione, utilizziamo l'area totale dell'immagine allineata
                val regionTotalPixels = bitmap.width * bitmap.height

                // 2. Inizializzazione ed esecuzione del TUO modello AI reale
                val aiDetector = AILesionDetector(requireContext())
                val result = aiDetector.analyzeImageAndCalculateBsa(bitmap, region, regionTotalPixels)

                // Chiudiamo l'interprete per evitare memory leak
                aiDetector.close()

                // 3. Torniamo sul Main Thread per aggiornare la UI e navigare
                withContext(Dispatchers.Main) {
                    viewModel.finalBsaResult.value = result
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