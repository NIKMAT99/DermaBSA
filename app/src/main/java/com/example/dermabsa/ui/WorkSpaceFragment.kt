package com.example.dermabsa.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.dermabsa.R
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
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
                // NUOVA SNACKBAR PER ERRORE GALLERIA
                Snackbar.make(requireView(), "Errore durante l'elaborazione dell'immagine", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.derma_text_dark))
                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    .show()
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


        // 2. LISTENER DEI BOTTONI
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
                // ECCO LA NUOVA SNACKBAR AL POSTO DEL TOAST
                Snackbar.make(requireView(), "Seleziona una zona prima di continuare", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.derma_teal))
                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    .show()

                return@setOnClickListener // Blocca l'esecuzione qui
            }

            // Salva la zona nel ViewModel e crea il bundle
            viewModel.selectedRegion.value = selectedRegion
            val bundle = bundleOf("REGION_KEY" to selectedRegion!!.name)

            // QUI CHIAMIAMO LA NOSTRA NUOVA FUNZIONE!
            mostraConsigli(bundle)
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
    } // <-- FINE DI onViewCreated (nota come si chiude qui!)

    // --- LE NOSTRE FUNZIONI DI SUPPORTO (Tutte fuori da onViewCreated) ---

    // Funzione di utilità per non ripetere il codice
    private fun aggiornaTesto(textView: TextView, testo: String) {
        textView.text = "Zona selezionata: $testo"
        textView.visibility = View.VISIBLE
    }

    private fun mostraConsigli(bundle: Bundle) {
        // Gonfiamo (carichiamo) il layout XML che abbiamo appena creato
        val dialogView = layoutInflater.inflate(R.layout.ic_dialog_info, null)

        // Creiamo un AlertDialog "vuoto" e ci infiliamo dentro il nostro layout
        val customDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        // FONDAMENTALE: rende lo sfondo del sistema trasparente per mostrare gli angoli arrotondati
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Recuperiamo i bottoni dal layout personalizzato
        val btnUnderstand = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_understand)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_cancel)

        // Se l'utente clicca "Ho capito" -> Chiudi il pop-up e chiedi da dove prendere la foto
        btnUnderstand.setOnClickListener {
            customDialog.dismiss()
            mostraSceltaSorgente(bundle)
        }

        // Se l'utente clicca "Annulla" -> Chiudi tutto e non fare nulla
        btnCancel.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()
    }

    private fun mostraSceltaSorgente(bundle: Bundle) {
        val options = arrayOf("Fotocamera", "Galleria")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Scegli sorgente immagine")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Andiamo direttamente alla fotocamera
                        findNavController().navigate(R.id.action_workspace_to_camera, bundle)
                    }
                    1 -> {
                        // Apre la galleria
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                }
            }
            .show()
    }

}