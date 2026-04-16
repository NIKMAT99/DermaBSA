package com.example.dermabsa.ui

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dermabsa.R
import com.example.dermabsa.model.BodyRegion
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class WorkspaceFragment : Fragment(R.layout.fragment_workspace) {

    private val viewModel: MainViewModel by activityViewModels()
    private var selectedRegion: BodyRegion? = null

    // Variabile per ricordare quale distretto è attualmente colorato di azzurro
    private var viewDistrettoAttiva: View? = null

    // Selettore Galleria con Fix per memoria e visibilità
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            try {
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

                // Ridimensionamento preventivo (max 1024px)
                val maxDim = 1024
                val scale = Math.min(maxDim.toFloat() / bitmap.width, maxDim.toFloat() / bitmap.height)
                val resized = Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)

                viewModel.patientPhoto.value = resized.copy(Bitmap.Config.ARGB_8888, true)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Il TextView che mostra il testo a schermo
        val tvZonaSelezionata = view.findViewById<TextView>(R.id.tv_zona_selezionata)
        val btnBack = view.findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener { findNavController().popBackStack() }

        // --- COLLEGAMENTO BOTTONI (LUND E BROWDER) ---
        // Ora passiamo "it" (che rappresenta il bottone appena cliccato) alla funzione seleziona

        // TESTA E COLLO
        view.findViewById<View>(R.id.btn_head_f).setOnClickListener {
            seleziona(BodyRegion.HEAD_FRONT, "Testa Anteriore (3.5%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_head_b).setOnClickListener {
            seleziona(BodyRegion.HEAD_BACK, "Testa Posteriore (3.5%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_neck_f).setOnClickListener {
            seleziona(BodyRegion.NECK_FRONT, "Collo Anteriore (1%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_neck_b).setOnClickListener {
            seleziona(BodyRegion.NECK_BACK, "Collo Posteriore (1%)", tvZonaSelezionata, it)
        }

        // TRONCO E SCHIENA
        // Anteriore
        view.findViewById<View>(R.id.btn_trunk_front).setOnClickListener {
            seleziona(BodyRegion.CHEST, "Torace (6.5%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_abdomen).setOnClickListener {
            seleziona(BodyRegion.ABDOMEN, "Addome (6.5%)", tvZonaSelezionata, it)
        }
        // Posteriore
        view.findViewById<View>(R.id.btn_upper_back).setOnClickListener {
            seleziona(BodyRegion.UPPER_BACK, "Schiena Sup. (6.5%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_lower_back).setOnClickListener {
            seleziona(BodyRegion.LOWER_BACK, "Schiena Inf. (6.5%)", tvZonaSelezionata, it)
        }

        // BRACCIA SUPERIORI
        view.findViewById<View>(R.id.btn_upper_arm_l_f).setOnClickListener {
            seleziona(BodyRegion.UPPER_ARM_LEFT_FRONT, "Braccio Sup. Sinistro Ant. (2%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_upper_arm_r_f).setOnClickListener {
            seleziona(BodyRegion.UPPER_ARM_RIGHT_FRONT, "Braccio Sup. Destro Ant. (2%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_upper_arm_l_b).setOnClickListener {
            seleziona(BodyRegion.UPPER_ARM_LEFT_BACK, "Braccio Sup. Sinistro Post. (2%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_upper_arm_r_b).setOnClickListener {
            seleziona(BodyRegion.UPPER_ARM_RIGHT_BACK, "Braccio Sup. Destro Post. (2%)", tvZonaSelezionata, it)
        }

        // AVAMBRACCIA
        view.findViewById<View>(R.id.btn_fore_arm_l_f).setOnClickListener {
            seleziona(BodyRegion.FOREARM_LEFT_FRONT, "Avambraccio Sinistro Ant. (1.5%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_fore_arm_r_f).setOnClickListener {
            seleziona(BodyRegion.FOREARM_RIGHT_FRONT, "Avambraccio Destro Ant. (1.5%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_fore_arm_l_b).setOnClickListener {
            seleziona(BodyRegion.FOREARM_LEFT_BACK, "Avambraccio Sinistro Post. (1.5%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_fore_arm_r_b).setOnClickListener {
            seleziona(BodyRegion.FOREARM_RIGHT_BACK, "Avambraccio Destro Post. (1.5%)", tvZonaSelezionata, it)
        }

        // MANI
        view.findViewById<View>(R.id.btn_hand_l_f).setOnClickListener {
            seleziona(BodyRegion.HAND_LEFT_FRONT, "Mano Sinistra Ant. (1.25%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_hand_r_f).setOnClickListener {
            seleziona(BodyRegion.HAND_RIGHT_FRONT, "Mano Destra Ant. (1.25%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_hand_l_b).setOnClickListener {
            seleziona(BodyRegion.HAND_LEFT_BACK, "Mano Sinistra Post. (1.25%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_hand_r_b).setOnClickListener {
            seleziona(BodyRegion.HAND_RIGHT_BACK, "Mano Destra Post. (1.25%)", tvZonaSelezionata, it)
        }

        // GENITALI E GLUTEI
        view.findViewById<View>(R.id.btn_genitals).setOnClickListener {
            seleziona(BodyRegion.GENITALS, "Genitali (1%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_buttock_l).setOnClickListener {
            seleziona(BodyRegion.BUTTOCK_LEFT, "Gluteo Sinistro (2.5%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_buttock_r).setOnClickListener {
            seleziona(BodyRegion.BUTTOCK_RIGHT, "Gluteo Destro (2.5%)", tvZonaSelezionata, it)
        }

        // COSCE
        view.findViewById<View>(R.id.btn_thigh_l_f).setOnClickListener {
            seleziona(BodyRegion.THIGH_LEFT_FRONT, "Coscia Sinistra Ant. (4.5%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_thigh_r_f).setOnClickListener {
            seleziona(BodyRegion.THIGH_RIGHT_FRONT, "Coscia Destra Ant. (4.5%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_thigh_l_b).setOnClickListener {
            seleziona(BodyRegion.THIGH_LEFT_BACK, "Coscia Sinistra Post. (4.5%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_thigh_r_b).setOnClickListener {
            seleziona(BodyRegion.THIGH_RIGHT_BACK, "Coscia Destra Post. (4.5%)", tvZonaSelezionata, it)
        }

        // GAMBE (STINCHI/POLPACCI)
        view.findViewById<View>(R.id.btn_leg_left_f).setOnClickListener {
            seleziona(BodyRegion.LOWER_LEG_LEFT_FRONT, "Gamba Sinistra Ant. (4%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_leg_r_f).setOnClickListener {
            seleziona(BodyRegion.LOWER_LEG_RIGHT_FRONT, "Gamba Destra Ant. (4%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_leg_left_b).setOnClickListener {
            seleziona(BodyRegion.LOWER_LEG_LEFT_BACK, "Gamba Sinistra Post. (4%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_leg_r_b).setOnClickListener {
            seleziona(BodyRegion.LOWER_LEG_RIGHT_BACK, "Gamba Destra Post. (4%)", tvZonaSelezionata, it)
        }

        // PIEDI
        view.findViewById<View>(R.id.btn_foot_l_f).setOnClickListener {
            seleziona(BodyRegion.FOOT_LEFT_FRONT, "Piede Sinistro Ant. (1.5%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_foot_r_f).setOnClickListener {
            seleziona(BodyRegion.FOOT_RIGHT_FRONT, "Piede Destro Ant. (1.5%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_foot_l_b).setOnClickListener {
            seleziona(BodyRegion.FOOT_LEFT_BACK, "Piede Sinistro Post. (1.5%)", tvZonaSelezionata, it)
        }
        view.findViewById<View>(R.id.btn_foot_r_b).setOnClickListener {
            seleziona(BodyRegion.FOOT_RIGHT_BACK, "Piede Destro Post. (1.5%)", tvZonaSelezionata, it)
        }

        // --- LOGICA DI CONFERMA E NAVIGAZIONE ---
        val btnConferma = view.findViewById<MaterialButton>(R.id.btn_conferma)
        btnConferma.setOnClickListener {
            if (selectedRegion == null) {
                Snackbar.make(requireView(), "Seleziona una zona prima di continuare", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.derma_teal))
                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    .show()

                return@setOnClickListener
            }

            viewModel.selectedRegion.value = selectedRegion
            val bundle = bundleOf("REGION_KEY" to selectedRegion!!.name)

            mostraConsigli(bundle)
        }

        // --- LOGICA TOGGLE FRONTE/RETRO ---
        val tabFronte = view.findViewById<TextView>(R.id.tab_fronte)
        val tabRetro = view.findViewById<TextView>(R.id.tab_retro)
        val layoutFront = view.findViewById<View>(R.id.layout_front_view)
        val layoutBack = view.findViewById<View>(R.id.layout_back_view)

        tabFronte.setOnClickListener {
            layoutFront.visibility = View.VISIBLE
            layoutBack.visibility = View.GONE
            tabFronte.setBackgroundResource(R.drawable.bg_toggle_selected)
            tabRetro.setBackgroundResource(R.drawable.bg_hotspot)
        }

        tabRetro.setOnClickListener {
            layoutFront.visibility = View.GONE
            layoutBack.visibility = View.VISIBLE
            tabRetro.setBackgroundResource(R.drawable.bg_toggle_selected)
            tabFronte.setBackgroundResource(R.drawable.bg_hotspot)
        }
    }

    // --- AGGIORNATA LA FUNZIONE SELEZIONA ---
    private fun seleziona(region: BodyRegion, nome: String, tv: TextView, viewCliccata: View) {
        selectedRegion = region
        tv.text = "Zona: $nome"
        tv.visibility = View.VISIBLE

        // Spegne il distretto precedente (toglie l'azzurro)
        viewDistrettoAttiva?.isSelected = false

        // Accende il distretto appena cliccato (fissa l'azzurro)
        viewCliccata.isSelected = true

        // Si ricorda qual è quello acceso per il prossimo click
        viewDistrettoAttiva = viewCliccata
    }

    private fun mostraConsigli(bundle: Bundle) {
        // FIX: Ora punta al layout corretto (ic_dialog_info) e non all'icona!
        val dialogView = layoutInflater.inflate(R.layout.ic_dialog_info, null)

        val customDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnUnderstand = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_understand)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_cancel)

        btnUnderstand.setOnClickListener {
            customDialog.dismiss()
            mostraSceltaSorgente(bundle)
        }

        btnCancel.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()
    }

    private fun mostraSceltaSorgente(bundle: Bundle) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_choose_source, null)

        val customDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCamera = dialogView.findViewById<MaterialButton>(R.id.btn_source_camera)
        val btnGallery = dialogView.findViewById<MaterialButton>(R.id.btn_source_gallery)

        btnCamera.setOnClickListener {
            customDialog.dismiss() // Chiudiamo prima il pop-up
            findNavController().navigate(R.id.action_workspace_to_camera, bundle)
        }

        btnGallery.setOnClickListener {
            customDialog.dismiss() // Chiudiamo prima il pop-up
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        customDialog.show()
    }
}