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
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import android.graphics.Bitmap
import com.example.dermabsa.model.BodyRegion

class PhotoConfirmFragment : Fragment(R.layout.fragment_photo_confirm) {

    // Recuperiamo i dati condivisi (foto scattata e zona scelta)
    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // UI Binding
        val alignmentView = view.findViewById<AlignmentView>(R.id.alignment_view_preview)
        val guideOverlay = view.findViewById<ImageView>(R.id.iv_confirm_guide_overlay)
        val sliderOpacity = view.findViewById<SeekBar>(R.id.slider_guide_opacity)
        val btnRetake = view.findViewById<MaterialButton>(R.id.btn_retake_photo)
        val btnAccept = view.findViewById<MaterialButton>(R.id.btn_accept_photo)
        val btnRotate = view.findViewById<View>(R.id.btn_rotate_photo)

        // --- NOVITÀ: CARICHIAMO L'OUTLINE CORRETTO IN BASE ALLA ZONA ---
        val region = viewModel.selectedRegion.value
        val overlayResId = when (region) {
            BodyRegion.HEAD_FRONT -> R.drawable.overlay_head_f
            BodyRegion.HEAD_BACK -> R.drawable.overlay_head_b
            BodyRegion.ARM_LEFT_FRONT -> R.drawable.overlay_arm_fsx
            BodyRegion.ARM_RIGHT_FRONT -> R.drawable.overlay_arm_fdx
            BodyRegion.ARM_LEFT_BACK -> R.drawable.overlay_arm_bsx
            BodyRegion.ARM_RIGHT_BACK -> R.drawable.overlay_arm_bdx
            BodyRegion.TRUNK_FRONT -> R.drawable.overlay_petto_f
            BodyRegion.UPPER_BACK -> R.drawable.overlay_tronco_b
            BodyRegion.ABDOMEN -> R.drawable.overlay_addome_f
            BodyRegion.LOWER_BACK -> R.drawable.overlay_lower_b
            BodyRegion.GENITALS -> R.drawable.overlay_gen
            BodyRegion.LEG_LEFT_FRONT -> R.drawable.overlay_leg_fsx
            BodyRegion.LEG_LEFT_BACK -> R.drawable.overlay_leg_bsx
            BodyRegion.LEG_RIGHT_FRONT -> R.drawable.overlay_leg_fdx
            BodyRegion.LEG_RIGHT_BACK -> R.drawable.overlay_leg_bdx
            else -> R.drawable.body_front
        }

        // Assegniamo la sagoma al livello trasparente dello slider!
        guideOverlay.setImageResource(overlayResId)

        // Carichiamo la foto nella Custom View (il livello sottostante)
        val photo = viewModel.patientPhoto.value
        if (photo != null) {
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
            val currentPhoto = viewModel.patientPhoto.value
            if (currentPhoto != null) {
                // 1. Creiamo la matrice matematica per girare di 90 gradi
                val matrix = android.graphics.Matrix()
                matrix.postRotate(90f)

                // 2. Creiamo una copia esatta della foto, ma ruotata!
                val rotatedPhoto = Bitmap.createBitmap(
                    currentPhoto, 0, 0, currentPhoto.width, currentPhoto.height, matrix, true
                )

                // 3. Aggiorniamo la memoria e diciamo alla Custom View di ricaricare tutto
                viewModel.patientPhoto.value = rotatedPhoto
                val emptyMap = BitmapFactory.decodeResource(resources, R.drawable.body_front)
                alignmentView.setImages(emptyMap, rotatedPhoto)
            }
        }

        btnAccept.setOnClickListener {
            val region = viewModel.selectedRegion.value

            if (region == null) {
                Toast.makeText(requireContext(), "Errore: Zona del corpo mancante", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cambiamo il testo del bottone e lo blocchiamo
            btnAccept.text = "Avvio..."
            btnAccept.isEnabled = false
            btnRetake.isEnabled = false

            // 1. Estraiamo l'immagine allineata dalla vista personalizzata
            val finalImage = alignmentView.getAlignedBitmap()

            // 2. IMPORTANTISSIMO: Aggiorniamo la foto nel ViewModel con quella RITAGLIATA!
            viewModel.patientPhoto.value = finalImage

            // 3. Navighiamo verso il caricamento. Sarà il LoadingFragment a gestire l'IA!
            findNavController().navigate(R.id.action_confirm_to_loading)
        }
    }

    override fun onResume() {
        super.onResume()
        // Se torniamo a questa schermata (es. dopo un errore), riabilitiamo i tasti!
        view?.let {
            val btnAccept = it.findViewById<MaterialButton>(R.id.btn_accept_photo)
            val btnRetake = it.findViewById<MaterialButton>(R.id.btn_retake_photo)
            btnAccept.text = "Conferma"
            btnAccept.isEnabled = true
            btnRetake.isEnabled = true
        }
    }
}