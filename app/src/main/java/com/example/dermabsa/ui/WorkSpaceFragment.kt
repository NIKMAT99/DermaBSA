package com.example.dermabsa.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.dermabsa.R
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import com.example.dermabsa.model.BodyRegion
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

class WorkspaceFragment : Fragment(R.layout.fragment_workspace) {

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            try {
                // 1. Caricamento della foto forzando il formato "Software" per renderla visibile
                val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                }

                // 2. Rimpiccioliamo l'immagine per non esaurire la memoria RAM (Max 1024px)
                val maxDimension = 1024
                val scale = Math.min(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height)
                val newWidth = Math.round(bitmap.width * scale)
                val newHeight = Math.round(bitmap.height * scale)

                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

                // 3. Copiamo in formato ARGB_8888 e salviamo nel ViewModel
                viewModel.patientPhoto.value = resizedBitmap.copy(Bitmap.Config.ARGB_8888, true)

                // 4. Prepariamo il bundle della zona e navighiamo alla conferma
                val bundle = bundleOf("REGION_KEY" to selectedRegion?.name)
                findNavController().navigate(R.id.action_workspace_to_confirm, bundle)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Errore elaborazione immagine", Toast.LENGTH_SHORT).show()
            }
        } else {
            // L'utente ha chiuso la galleria senza scegliere nulla
        }
    }

    // Variabile per memorizzare la zona scelta
    private val viewModel: MainViewModel by activityViewModels()

    private var selectedRegion: BodyRegion? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Il TextView che mostra il testo a schermo
        val tvZonaSelezionata = view.findViewById<TextView>(R.id.tv_zona_selezionata)

        // 1. COLLEGAMENTO DEI BOTTONI
        val btnHeadF = view.findViewById<View>(R.id.btn_head_f)
        val btnHeadB = view.findViewById<View>(R.id.btn_head_b)
        val btnArmLeftF = view.findViewById<View>(R.id.btn_arm_left_f)
        val btnArmRightF = view.findViewById<View>(R.id.btn_arm_right_f)
        val btnArmLeftB = view.findViewById<View>(R.id.btn_arm_left_b)
        val btnArmRightB = view.findViewById<View>(R.id.btn_arm_right_b)
        val btnTrunkFront = view.findViewById<View>(R.id.btn_trunk_front)
        val btnUpperBack = view.findViewById<View>(R.id.btn_upper_back)
        val btnAbdomen = view.findViewById<View>(R.id.btn_abdomen)
        val btnLowerBack = view.findViewById<View>(R.id.btn_lower_back)
        val btnGenitals = view.findViewById<View>(R.id.btn_genitals)
        val btnLegLeftF = view.findViewById<View>(R.id.btn_leg_left_f)
        val btnLegLeftB = view.findViewById<View>(R.id.btn_leg_left_b)
        val btnLegRightF = view.findViewById<View>(R.id.btn_leg_right_f)
        val btnLegRightB = view.findViewById<View>(R.id.btn_leg_right_b)


        // 2. LISTENER DEI BOTTONI (Puliti dai Toast)
        btnHeadF.setOnClickListener {
            selectedRegion = BodyRegion.HEAD_FRONT
            aggiornaTesto(tvZonaSelezionata, "Testa anteriore (4.5%)")
        }
        btnHeadB.setOnClickListener {
            selectedRegion = BodyRegion.HEAD_BACK
            aggiornaTesto(tvZonaSelezionata, "Testa posteriore (4.5%)")
        }
        btnArmLeftF.setOnClickListener {
            selectedRegion = BodyRegion.ARM_LEFT_FRONT
            aggiornaTesto(tvZonaSelezionata, "Braccio sinistro anteriore (4.5%)")
        }
        btnArmRightF.setOnClickListener {
            selectedRegion = BodyRegion.ARM_RIGHT_FRONT
            aggiornaTesto(tvZonaSelezionata, "Braccio destro anteriore (4.5%)")
        }
        btnArmLeftB.setOnClickListener {
            selectedRegion = BodyRegion.ARM_LEFT_BACK
            aggiornaTesto(tvZonaSelezionata, "Braccio sinistro posteriore (4.5%)")
        }
        btnArmRightB.setOnClickListener {
            selectedRegion = BodyRegion.ARM_RIGHT_BACK
            aggiornaTesto(tvZonaSelezionata, "Braccio destro posteriore (4.5%)")
        }
        btnTrunkFront.setOnClickListener {
            selectedRegion = BodyRegion.TRUNK_FRONT
            aggiornaTesto(tvZonaSelezionata, "Tronco anteriore (9%)")
        }
        btnUpperBack.setOnClickListener {
            selectedRegion = BodyRegion.UPPER_BACK
            aggiornaTesto(tvZonaSelezionata, "Tronco posteriore (9%)")
        }
        btnAbdomen.setOnClickListener {
            selectedRegion = BodyRegion.ABDOMEN
            aggiornaTesto(tvZonaSelezionata, "Addome (9%)")
        }
        btnLowerBack.setOnClickListener {
            selectedRegion = BodyRegion.LOWER_BACK
            aggiornaTesto(tvZonaSelezionata, "Lombare (9%)")
        }
        btnGenitals.setOnClickListener {
            selectedRegion = BodyRegion.GENITALS
            aggiornaTesto(tvZonaSelezionata, "Genitali (1%)")
        }
        btnLegLeftF.setOnClickListener {
            selectedRegion = BodyRegion.LEG_LEFT_FRONT
            aggiornaTesto(tvZonaSelezionata, "Gamba sinistra anteriore (9%)")
        }
        btnLegLeftB.setOnClickListener {
            selectedRegion = BodyRegion.LEG_LEFT_BACK
            aggiornaTesto(tvZonaSelezionata, "Gamba sinistra posteriore (9%)")
        }
        btnLegRightF.setOnClickListener {
            selectedRegion = BodyRegion.LEG_RIGHT_FRONT
            aggiornaTesto(tvZonaSelezionata, "Gamba destra anteriore (9%)")
        }
        btnLegRightB.setOnClickListener {
            selectedRegion = BodyRegion.LEG_RIGHT_BACK
            aggiornaTesto(tvZonaSelezionata, "Gamba destra posteriore (9%)")
        }

        val btnConferma = view.findViewById<MaterialButton>(R.id.btn_conferma)

        btnConferma.setOnClickListener {
            // Controlla che la zona sia stata scelta!
            if (selectedRegion == null) {
                Toast.makeText(requireContext(), "Seleziona una zona prima di continuare", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Blocca l'esecuzione qui
            }

            // Salva la zona nel ViewModel
            viewModel.selectedRegion.value = selectedRegion
            val bundle = bundleOf("REGION_KEY" to selectedRegion!!.name)

            val options = arrayOf("Scatta una foto", "Seleziona dalla galleria")

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Scegli sorgente immagine")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Ricorda bene")
                                .setMessage("Per aiutare l'AI a valutare correttamente la psoriasi, assicurati di:\n\n" +
                                        "Avere un'ottima illuminazione (luce naturale o molto chiara).\n\n" +
                                        "Usare uno sfondo neutro e pulito dietro di te.\n\n" +
                                        "Tenere il telefono fermo per una messa a fuoco nitida.")
                                .setPositiveButton("Ho capito!") { innerDialog, _ ->
                                    // Andiamo alla fotocamera e passiamo il bundle!
                                    findNavController().navigate(R.id.action_workspace_to_camera, bundle)
                                }
                                .setNegativeButton("Annulla", null)
                                .show()
                        }
                        1 -> {
                            // Apre la galleria sicura
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    }
                }
                .show()
        }

        // --- LOGICA TOGGLE FRONTE/RETRO ---
        val tabFronte = view.findViewById<TextView>(R.id.tab_fronte)
        val tabRetro = view.findViewById<TextView>(R.id.tab_retro)
        val layoutFront = view.findViewById<View>(R.id.layout_front_view)
        val layoutBack = view.findViewById<View>(R.id.layout_back_view)

        tabFronte.setOnClickListener {
            // Mostra Fronte, Nascondi Retro
            layoutFront.visibility = View.VISIBLE
            layoutBack.visibility = View.GONE
            // Cambia stile
            tabFronte.setBackgroundResource(R.drawable.bg_toggle_selected)
            tabRetro.setBackgroundResource(R.drawable.bg_hotspot)
        }

        tabRetro.setOnClickListener {
            // Mostra Retro, Nascondi Fronte
            layoutFront.visibility = View.GONE
            layoutBack.visibility = View.VISIBLE
            // Cambia stile
            tabRetro.setBackgroundResource(R.drawable.bg_toggle_selected)
            tabFronte.setBackgroundResource(R.drawable.bg_hotspot)
        }
    }

    // Funzione di utilità per non ripetere il codice
    private fun aggiornaTesto(textView: TextView, testo: String) {
        textView.text = "Zona selezionata: $testo"
        textView.visibility = View.VISIBLE
    }
}