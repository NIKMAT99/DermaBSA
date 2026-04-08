package com.example.dermabsa.ui

import com.example.dermabsa.R
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dermabsa.ui.AlignmentView // Assicurati che il nome sia corretto con quello del tuo progetto
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

        val btnBack = view.findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

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
            // Immagine fittizia trasparente come "mappa di sfondo"
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

        // Bottone Ruota
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

            // Cambiamo il testo del bottone e li blocchiamo per evitare doppi click
            btnAccept.text = "Avvio..."
            btnAccept.isEnabled = false
            btnRetake.isEnabled = false

            // Salviamo in memoria il Navigatore per poterlo usare mentre siamo in background
            val navController = findNavController()

            // 1. Estraiamo l'immagine allineata MENTRE siamo ancora su questa pagina
            val finalImage = alignmentView.getAlignedBitmap()
            val regionTotalPixels = finalImage.width * finalImage.height

            lifecycleScope.launch {
                // 2. NAVIGAZIONE 1: Andiamo subito al LoadingFragment così l'utente vede lo spinner girare!
                navController.navigate(R.id.action_confirm_to_loading)

                // 3. Ora che l'utente vede il caricamento, eseguiamo l'IA pesante in background
                val result = withContext(Dispatchers.Default) {
                    aiLesionDetector.analyzeImageAndCalculateBsa(
                        alignedImage = finalImage,
                        region = region,
                        regionTotalPixels = regionTotalPixels
                    )
                }

                // 4. L'IA ha finito! Salviamo il risultato...
                viewModel.finalBsaResult.value = result

                // 5. NAVIGAZIONE 2: Spostiamoci automaticamente alla schermata del Risultato!
                navController.navigate(R.id.action_loading_to_result)
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