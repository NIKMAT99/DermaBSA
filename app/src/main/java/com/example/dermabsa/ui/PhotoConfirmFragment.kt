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
import androidx.core.content.ContextCompat
import com.example.dermabsa.model.BodyRegion
import com.google.android.material.snackbar.Snackbar

class PhotoConfirmFragment : Fragment(R.layout.fragment_photo_confirm) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // UI Binding
        val alignmentView = view.findViewById<AlignmentView>(R.id.alignment_view_preview)
        val sliderOpacity = view.findViewById<SeekBar>(R.id.slider_guide_opacity)
        val guideOverlay = view.findViewById<ImageView>(R.id.iv_confirm_guide_overlay)
        val btnRetake = view.findViewById<MaterialButton>(R.id.btn_retake_photo)
        val btnAccept = view.findViewById<MaterialButton>(R.id.btn_accept_photo)
        val btnRotate = view.findViewById<View>(R.id.btn_rotate_photo)

        // CHIAMIAMO LA MAGIA DINAMICA QUI!
        impostaOverlayDinamico(view)

        // Carichiamo la foto nella Custom View (il livello sottostante)
        val photo = viewModel.patientPhoto.value
        if (photo != null) {
            alignmentView.setImages(null, photo)
        } else {
            Snackbar.make(requireView(), "Errore: Foto non trovata", Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.derma_text_dark))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                .show()
        }

        // --- GESTIONE DEI CONTROLLI UI ---

        // Slider Opacità Guida
        sliderOpacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                guideOverlay.alpha = progress / 100f
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        guideOverlay.alpha = sliderOpacity.progress / 100f
        // Bottone Rifai: torna indietro alla schermata Scan
        btnRetake.setOnClickListener {
            findNavController().popBackStack()
        }

        // Bottone Ruota
        btnRotate.setOnClickListener {
            val currentPhoto = viewModel.patientPhoto.value
            if (currentPhoto != null) {
                // 1. Creiamo la matrice per girare di 90 gradi
                val matrix = android.graphics.Matrix()
                matrix.postRotate(90f)

                // 2. Creiamo la foto ruotata
                val rotatedPhoto = Bitmap.createBitmap(
                    currentPhoto, 0, 0, currentPhoto.width, currentPhoto.height, matrix, true
                )

                // 3. Aggiorniamo il ViewModel
                viewModel.patientPhoto.value = rotatedPhoto

                // 4. Aggiorniamo la vista passando NULL al posto della sagoma bianca
                alignmentView.setImages(null, rotatedPhoto)
            }
        }

        btnAccept.setOnClickListener {
            val region = viewModel.selectedRegion.value

            if (region == null) {
                // NUOVA SNACKBAR AL POSTO DEL TOAST
                Snackbar.make(requireView(), "Errore: Zona del corpo mancante", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.derma_text_dark))
                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    .show()

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
        view?.let {
            val btnAccept = it.findViewById<MaterialButton>(R.id.btn_accept_photo)
            val btnRetake = it.findViewById<MaterialButton>(R.id.btn_retake_photo)
            btnAccept.text = "Conferma"
            btnAccept.isEnabled = true
            btnRetake.isEnabled = true
        }
    }

    private fun impostaOverlayDinamico(view: View) {
        val overlayImg = view.findViewById<ImageView>(R.id.iv_confirm_guide_overlay)
        val region = viewModel.selectedRegion.value

        // Funzione per convertire DP in Pixel
        val scale = resources.displayMetrics.density
        fun dpToPx(dp: Int): Int = (dp * scale + 0.5f).toInt()

        var resId = R.drawable.overlay_petto_f
        var paddingInDp = 50

        // Usiamo direttamente l'Enum BodyRegion! Molto più pulito.
        when (region) {
            BodyRegion.HEAD_FRONT -> { resId = R.drawable.overlay_head_f; paddingInDp = 20 }
            BodyRegion.HEAD_BACK -> { resId = R.drawable.overlay_head_b; paddingInDp = 20 }
            BodyRegion.NECK_FRONT -> { resId = R.drawable.overlay_neck_f; paddingInDp = 20 }
            BodyRegion.NECK_BACK -> { resId = R.drawable.overlay_neck_b; paddingInDp = 20 }

            BodyRegion.CHEST -> { resId = R.drawable.overlay_petto_f; paddingInDp = 10 }
            BodyRegion.ABDOMEN -> { resId = R.drawable.overlay_addome_f; paddingInDp = 10 }
            BodyRegion.UPPER_BACK -> { resId = R.drawable.overlay_tronco_b; paddingInDp = 10 }
            BodyRegion.LOWER_BACK -> { resId = R.drawable.overlay_lower_b; paddingInDp = 10 }

            BodyRegion.UPPER_ARM_LEFT_FRONT -> { resId = R.drawable.overlay_upper_arm_fsx; paddingInDp = 10 }
            BodyRegion.UPPER_ARM_RIGHT_FRONT -> { resId = R.drawable.overlay_upper_arm_fdx; paddingInDp = 10 }
            BodyRegion.UPPER_ARM_LEFT_BACK -> { resId = R.drawable.overlay_upper_arm_bsx; paddingInDp = 10 }
            BodyRegion.UPPER_ARM_RIGHT_BACK -> { resId = R.drawable.overlay_upper_arm_bdx; paddingInDp = 10 }
            BodyRegion.FOREARM_LEFT_FRONT -> { resId = R.drawable.overlay_forearm_fsx; paddingInDp = 10 }
            BodyRegion.FOREARM_RIGHT_FRONT -> { resId = R.drawable.overlay_forearm_fdx; paddingInDp = 10 }
            BodyRegion.FOREARM_LEFT_BACK -> { resId = R.drawable.overlay_forearm_bsx; paddingInDp = 10 }
            BodyRegion.FOREARM_RIGHT_BACK -> { resId = R.drawable.overlay_forearm_bdx; paddingInDp = 10 }

            BodyRegion.HAND_LEFT_FRONT -> { resId = R.drawable.overlay_hand_fsx; paddingInDp = 20 }
            BodyRegion.HAND_RIGHT_FRONT -> { resId = R.drawable.overlay_hand_fdx; paddingInDp = 20 }
            BodyRegion.HAND_LEFT_BACK -> { resId = R.drawable.overlay_hand_bsx; paddingInDp = 20 }
            BodyRegion.HAND_RIGHT_BACK -> { resId = R.drawable.overlay_hand_bdx; paddingInDp = 20 }

            BodyRegion.GENITALS -> { resId = R.drawable.overlay_gen; paddingInDp = 10 }
            BodyRegion.BUTTOCK_LEFT -> { resId = R.drawable.overlay_buttock_sx; paddingInDp = 20 }
            BodyRegion.BUTTOCK_RIGHT -> { resId = R.drawable.overlay_buttock_dx; paddingInDp = 20 }

            BodyRegion.THIGH_LEFT_FRONT -> { resId = R.drawable.overlay_thigh_fsx; paddingInDp = 20 }
            BodyRegion.THIGH_RIGHT_FRONT -> { resId = R.drawable.overlay_thigh_fdx; paddingInDp = 20 }
            BodyRegion.THIGH_LEFT_BACK -> { resId = R.drawable.overlay_thigh_bsx; paddingInDp = 20 }
            BodyRegion.THIGH_RIGHT_BACK -> { resId = R.drawable.overlay_thigh_bdx; paddingInDp = 20 }
            BodyRegion.LOWER_LEG_LEFT_FRONT -> { resId = R.drawable.overlay_leg_fsx; paddingInDp = 10 }
            BodyRegion.LOWER_LEG_RIGHT_FRONT -> { resId = R.drawable.overlay_leg_fdx; paddingInDp = 10 }
            BodyRegion.LOWER_LEG_LEFT_BACK -> { resId = R.drawable.overlay_leg_bsx; paddingInDp = 10 }
            BodyRegion.LOWER_LEG_RIGHT_BACK -> { resId = R.drawable.overlay_leg_bdx; paddingInDp = 10 }

            BodyRegion.FOOT_LEFT_FRONT -> { resId = R.drawable.overlay_foot_fsx; paddingInDp = 20 }
            BodyRegion.FOOT_RIGHT_FRONT -> { resId = R.drawable.overlay_foot_fdx; paddingInDp = 20 }
            BodyRegion.FOOT_LEFT_BACK -> { resId = R.drawable.overlay_foot_bsx; paddingInDp = 20 }
            BodyRegion.FOOT_RIGHT_BACK -> { resId = R.drawable.overlay_foot_bdx; paddingInDp = 20 }

            else -> { resId = R.drawable.overlay_petto_f; paddingInDp = 50 }
        }

        // Applichiamo immagine e margini dinamici
        overlayImg.setImageResource(resId)
        val p = dpToPx(paddingInDp)
        overlayImg.setPadding(p, p, p, p)
    }


}