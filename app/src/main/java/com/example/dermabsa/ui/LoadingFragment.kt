package com.example.dermabsa.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.dermabsa.R
import com.example.dermabsa.utils.AILesionDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoadingFragment : Fragment(R.layout.fragment_loading) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scanLine = view.findViewById<View>(R.id.scan_line)
        val tvStatus = view.findViewById<TextView>(R.id.tv_loading_status)
        val silhouette = view.findViewById<View>(R.id.iv_scan_silhouette)

        // --- 1. ANIMAZIONE DEL LASER (Invariata) ---
        silhouette.post {
            val altezzaOmino = silhouette.height.toFloat()
            val animator = ObjectAnimator.ofFloat(scanLine, "translationY", 0f, altezzaOmino)
            animator.duration = 1000 // Laser più veloce per matchare il nuovo tempo
            animator.repeatCount = ValueAnimator.INFINITE
            animator.repeatMode = ValueAnimator.REVERSE
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.start()
        }

        val safeContext = requireContext()

        // --- 2. LOGICA AI + ANIMAZIONE RAPIDA ---
        viewLifecycleOwner.lifecycleScope.launch {

            // Job per cambiare le scritte velocemente (ogni 0.5 secondi)
            val textAnimationJob = launch {
                val fasiDiElaborazione = listOf(
                    "Allineamento...",
                    "Scansione...",
                    "Rilevamento...",
                    "Calcolo BSA..."
                )
                var index = 0
                while (true) {
                    tvStatus.text = fasiDiElaborazione[index % fasiDiElaborazione.size]
                    index++
                    delay(500)
                }
            }

            try {
                withContext(Dispatchers.Default) {
                    val bitmap = viewModel.patientPhoto.value
                        ?: throw IllegalStateException("Immagine non trovata.")
                    val region = viewModel.selectedRegion.value
                        ?: throw IllegalStateException("Distretto non selezionato.")

                    val regionTotalPixels = bitmap.width * bitmap.height

                    // Esecuzione AI reale
                    val aiDetector = AILesionDetector(safeContext)
                    val (result, highlightedBitmap) = aiDetector.analyzeImageAndCalculateBsa(bitmap, region, regionTotalPixels)
                    aiDetector.close()

                    // --- IL TUO NUOVO TEMPO DI ATTESA (MAX 1 SECONDO) ---
                    delay(1000)

                    withContext(Dispatchers.Main) {
                        viewModel.finalBsaResult.value = result
                        viewModel.patientPhoto.value = highlightedBitmap
                    }
                }

                textAnimationJob.cancel()
                tvStatus.text = "Completato!"
                delay(300) // Pausa brevissima finale

                findNavController().navigate(R.id.action_loading_to_result)

            }  catch (e: Exception) {
            textAnimationJob.cancel()
            withContext(Dispatchers.Main) {
                // TRUCCO: Troviamo la "radice" dell'app così la Snackbar sopravvive quando cambiamo pagina
                val rootView = requireActivity().findViewById<View>(android.R.id.content)

                Snackbar.make(rootView, "Errore: ${e.message}", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(ContextCompat.getColor(safeContext, R.color.derma_text_dark)) // Grigio scuro per l'errore
                    .setTextColor(ContextCompat.getColor(safeContext, R.color.white))
                    .show()

                findNavController().popBackStack()
            }
        }
        }
    }
}