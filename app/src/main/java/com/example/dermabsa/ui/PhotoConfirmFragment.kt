package com.example.dermabsa.ui

import android.graphics.Bitmap
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
import com.example.dermabsa.ui.AlignmentView
import com.google.android.material.button.MaterialButton

class PhotoConfirmFragment : Fragment(R.layout.fragment_photo_confirm) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI Binding
        val btnBack = view.findViewById<ImageButton>(R.id.btn_back)
        val alignmentView = view.findViewById<AlignmentView>(R.id.alignment_view_preview)
        val guideOverlay = view.findViewById<ImageView>(R.id.iv_confirm_guide_overlay)
        val sliderOpacity = view.findViewById<SeekBar>(R.id.slider_guide_opacity)
        val btnRetake = view.findViewById<MaterialButton>(R.id.btn_retake_photo)
        val btnAccept = view.findViewById<MaterialButton>(R.id.btn_accept_photo)
        val btnRotate = view.findViewById<View>(R.id.btn_rotate_photo)

        // --- 1. SETTA L'OVERLAY DINAMICO ---
        // Aggiungiamo .name per trasformare il BodyRegion di nuovo in testo!
        val regioneScelta = viewModel.selectedRegion.value?.name
            ?: arguments?.getString("REGION_KEY")
            ?: "TRUNK_FRONT"

        val resId = when (regioneScelta) {
            "HEAD_FRONT" -> R.drawable.overlay_head_f
            "HEAD_BACK" -> R.drawable.overlay_head_b
            "TRUNK_FRONT" -> R.drawable.overlay_petto_f
            "ABDOMEN" -> R.drawable.overlay_addome_f
            "UPPER_BACK" -> R.drawable.overlay_tronco_b
            "LOWER_BACK" -> R.drawable.overlay_lower_b
            "ARM_LEFT_FRONT" -> R.drawable.overlay_arm_fsx
            "ARM_RIGHT_FRONT" -> R.drawable.overlay_arm_fdx
            "ARM_LEFT_BACK" -> R.drawable.overlay_arm_bsx
            "ARM_RIGHT_BACK" -> R.drawable.overlay_arm_bdx
            "LEG_LEFT_FRONT" -> R.drawable.overlay_leg_fsx
            "LEG_RIGHT_FRONT" -> R.drawable.overlay_leg_fdx
            "LEG_LEFT_BACK" -> R.drawable.overlay_leg_bsx
            "LEG_RIGHT_BACK" -> R.drawable.overlay_leg_bdx
            "GENITALS" -> R.drawable.overlay_gen
            else -> R.drawable.overlay_petto_f
        }
        guideOverlay.setImageResource(resId)

        // --- 2. CARICAMENTO IMMAGINI NELLA CUSTOM VIEW ---
        val photo = viewModel.patientPhoto.value
        if (photo != null) {
            // Creiamo una piccola bitmap trasparente 1x1 invece di caricare 'body_front'
            val emptyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            alignmentView.setImages(emptyBitmap, photo)
        } else {
            Toast.makeText(requireContext(), "Errore: Foto non trovata", Toast.LENGTH_SHORT).show()
        }

        // --- 3. GESTIONE OPACITÀ (SeekBar) ---
        // Sincronizziamo l'opacità iniziale con il valore attuale della SeekBar
        guideOverlay.alpha = sliderOpacity.progress / 100f

        sliderOpacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Converte il progresso 0-100 in opacità 0.0-1.0
                guideOverlay.alpha = progress / 100f
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // --- 4. ALTRI CONTROLLI ---
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnRetake.setOnClickListener {
            findNavController().popBackStack()
        }

        btnRotate.setOnClickListener {
            alignmentView.rotate90()
        }

        btnAccept.setOnClickListener {
            val region = viewModel.selectedRegion.value

            if (region == null) {
                Toast.makeText(requireContext(), "Errore: Zona del corpo mancante", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnAccept.text = "Avvio..."
            btnAccept.isEnabled = false
            btnRetake.isEnabled = false

            // Estraiamo l'immagine allineata e aggiorniamo il ViewModel
            val finalImage = alignmentView.getAlignedBitmap()
            viewModel.patientPhoto.value = finalImage

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
}