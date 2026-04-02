package com.example.dermabsa.ui

import com.example.dermabsa.R
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dermabsa.ui.AlignmentView
import com.example.dermabsa.utils.AILesionDetector
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotoConfirmFragment : Fragment(R.layout.fragment_photo_confirm) {

    // Recuperiamo i dati condivisi (foto scattata e zona scelta)
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var aiLesionDetector: AILesionDetector

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inizializza l'IA
        aiLesionDetector = AILesionDetector(requireContext())

        // UI Binding
        val alignmentView = view.findViewById<AlignmentView>(R.id.alignment_view_preview)
        val guideOverlay = view.findViewById<ImageView>(R.id.iv_confirm_guide_overlay)
        val sliderOpacity = view.findViewById<SeekBar>(R.id.slider_guide_opacity)
        val btnRetake = view.findViewById<MaterialButton>(R.id.btn_retake_photo)
        val btnAccept = view.findViewById<MaterialButton>(R.id.btn_accept_photo)
        val btnRotate = view.findViewById<View>(R.id.btn_rotate_photo)

        // Carichiamo le immagini nella Custom View
        val photo = viewModel.patientPhoto.value
        if (photo != null) {
            // Nota: qui mettiamo un'immagine fittizia trasparente come "mappa di sfondo"
            // perché l'overlay lo sta già gestendo la tua compagna con 'guideOverlay'
            val emptyMap = BitmapFactory.decodeResource(resources, R.drawable.body_front)
            alignmentView.setImages(emptyMap, photo)
        } else {
            Toast.makeText(requireContext(), "Errore: Foto non trovata", Toast.LENGTH_SHORT).show()
        }

        // --- GESTIONE DEI CONTROLLI UI ---

        // Slider Opacità Guida
        sliderOpacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Converte il progresso 0-100 in opacità 0.0-1.0
                guideOverlay.alpha = progress / 100f
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Bottone Rifai: torna indietro alla schermata Scan
        btnRetake.setOnClickListener {
            findNavController().popBackStack()
        }

        // Bottone Ruota (opzionale, se vuoi ruotare l'ImageView o la Matrix)
        btnRotate.setOnClickListener {
            Toast.makeText(requireContext(), "Usa due dita per ruotare l'immagine!", Toast.LENGTH_SHORT).show()
        }

        // --- IL MOTORE DI CALCOLO (Bottone Approva) ---
        btnAccept.setOnClickListener {
            val region = viewModel.selectedRegion.value

            if (region == null) {
                Toast.makeText(requireContext(), "Errore: Zona del corpo mancante", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cambiamo il testo del bottone per dare feedback all'utente
            btnAccept.text = "Analisi in corso..."
            btnAccept.isEnabled = false
            btnRetake.isEnabled = false

            lifecycleScope.launch {
                val result = withContext(Dispatchers.Default) {
                    // 1. Estraiamo l'immagine allineata dal tuo Canvas
                    val finalImage = alignmentView.getAlignedBitmap()

                    // 2. Calcoliamo l'area totale in pixel (l'intera vista)
                    val regionTotalPixels = finalImage.width * finalImage.height

                    // 3. Eseguiamo l'IA!
                    aiLesionDetector.analyzeImageAndCalculateBsa(
                        alignedImage = finalImage,
                        region = region,
                        regionTotalPixels = regionTotalPixels
                    )
                }

                // 4. Salviamo il risultato e navighiamo alla pagina finale
                viewModel.finalBsaResult.value = result
                findNavController().navigate(R.id.action_photoConfirmFragment_to_resultFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::aiLesionDetector.isInitialized) {
            aiLesionDetector.close()
        }
    }
}