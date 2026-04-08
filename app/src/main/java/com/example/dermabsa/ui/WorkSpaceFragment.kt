package com.example.dermabsa.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.dermabsa.R
import android.widget.ImageButton
import com.example.dermabsa.model.BodyRegion
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf

class WorkspaceFragment : Fragment(R.layout.fragment_workspace) {

    // Variabile per memorizzare la zona scelta
    private var selectedRegion: BodyRegion? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Il TextView che mostra il testo a schermo
        val tvZonaSelezionata = view.findViewById<TextView>(R.id.tv_zona_selezionata)

        // 1. COLLEGAMENTO DEI BOTTONI (Sostituisci gli R.id.* con quelli corretti del tuo XML)
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

        val btnConferma = view.findViewById<View>(R.id.btn_conferma)


        // 2. LISTENER DEI BOTTONI (I tuoi click riadattati per il Fragment)
        btnHeadF.setOnClickListener {
            selectedRegion = BodyRegion.HEAD_FRONT
            Toast.makeText(requireContext(), "Selezionata: Testa anteriore (4.5%)", Toast.LENGTH_SHORT).show()
            aggiornaTesto(tvZonaSelezionata, "Testa anteriore (4.5%)")
        }
        btnHeadB.setOnClickListener {
            selectedRegion = BodyRegion.HEAD_BACK
            Toast.makeText(requireContext(), "Selezionata: Testa posteriore (4.5%)", Toast.LENGTH_SHORT).show()
            aggiornaTesto(tvZonaSelezionata, "Testa posteriore (4.5%)")
        }
        btnArmLeftF.setOnClickListener {
            selectedRegion = BodyRegion.ARM_LEFT_FRONT
            Toast.makeText(requireContext(), "Selezionata: Braccio sinistro anteriore (4.5%)", Toast.LENGTH_SHORT).show()
            aggiornaTesto(tvZonaSelezionata, "Braccio sinistro anteriore (4.5%)")
        }
        btnArmRightF.setOnClickListener {
            selectedRegion = BodyRegion.ARM_RIGHT_FRONT
            Toast.makeText(requireContext(), "Selezionata: Braccio destro anteriore (4.5%)", Toast.LENGTH_SHORT).show()
            aggiornaTesto(tvZonaSelezionata, "Braccio destro anteriore (4.5%)")
        }
        btnArmLeftB.setOnClickListener {
            selectedRegion = BodyRegion.ARM_LEFT_BACK
            Toast.makeText(requireContext(), "Selezionata: Braccio sinistro posteriore (4.5%)", Toast.LENGTH_SHORT).show()
            aggiornaTesto(tvZonaSelezionata, "Braccio sinistro posteriore (4.5%)")
        }
        btnArmRightB.setOnClickListener {
            selectedRegion = BodyRegion.ARM_RIGHT_BACK
            Toast.makeText(requireContext(), "Selezionata: Braccio destro posteriore (4.5%)", Toast.LENGTH_SHORT).show()
            aggiornaTesto(tvZonaSelezionata, "Braccio destro posteriore (4.5%)")
        }
        btnTrunkFront.setOnClickListener {
            selectedRegion = BodyRegion.TRUNK_FRONT
            Toast.makeText(requireContext(), "Selezionata: Tronco anteriore (9%)", Toast.LENGTH_SHORT).show()
            aggiornaTesto(tvZonaSelezionata, "Tronco anteriore (9%)")
        }
        btnUpperBack.setOnClickListener {
            selectedRegion = BodyRegion.UPPER_BACK
            Toast.makeText(requireContext(), "Selezionata: Tronco posteriore (9%)", Toast.LENGTH_SHORT).show()
            aggiornaTesto(tvZonaSelezionata, "Tronco posteriore (9%)")
        }
        btnAbdomen.setOnClickListener {
            selectedRegion = BodyRegion.ABDOMEN
            Toast.makeText(requireContext(), "Selezionata: Addome (9%)", Toast.LENGTH_SHORT).show()
            aggiornaTesto(tvZonaSelezionata, "Addome (9%)")
        }
        btnLowerBack.setOnClickListener {
            selectedRegion = BodyRegion.LOWER_BACK
            Toast.makeText(requireContext(), "Selezionata: Lombare (9%)", Toast.LENGTH_SHORT).show()
            aggiornaTesto(tvZonaSelezionata, "Lombare (9%)")
        }
        btnGenitals.setOnClickListener {
            selectedRegion = BodyRegion.GENITALS
            Toast.makeText(requireContext(), "Selezionata: Genitali (1%)", Toast.LENGTH_SHORT).show()
            aggiornaTesto(tvZonaSelezionata, "Genitali (1%)")
        }
        btnLegLeftF.setOnClickListener {
            selectedRegion = BodyRegion.LEG_LEFT_FRONT
            Toast.makeText(requireContext(), "Selezionata: Gamba sinistra anteriore (9%)", Toast.LENGTH_SHORT).show()
            aggiornaTesto(tvZonaSelezionata, "Gamba sinistra anteriore (9%)")
        }
        btnLegLeftB.setOnClickListener {
            selectedRegion = BodyRegion.LEG_LEFT_BACK
            Toast.makeText(requireContext(), "Selezionata: Gamba sinistra posteriore (9%)", Toast.LENGTH_SHORT).show()
            aggiornaTesto(tvZonaSelezionata, "Gamba sinistra posteriore (9%)")
        }
        btnLegRightF.setOnClickListener {
            selectedRegion = BodyRegion.LEG_RIGHT_FRONT
            Toast.makeText(requireContext(), "Selezionata: Gamba destra anteriore (9%)", Toast.LENGTH_SHORT).show()
            aggiornaTesto(tvZonaSelezionata, "Gamba destra anteriore (9%)")
        }
        btnLegRightB.setOnClickListener {
            selectedRegion = BodyRegion.LEG_RIGHT_BACK
            Toast.makeText(requireContext(), "Selezionata: Gamba destra posteriore (9%)", Toast.LENGTH_SHORT).show()
            aggiornaTesto(tvZonaSelezionata, "Gamba destra posteriore (9%)")
        }

        // Tasto di conferma in fondo
        btnConferma.setOnClickListener {
            if (selectedRegion != null) {
                // Passiamo il NOME della regione (es. "TRUNK_FRONT") al prossimo Fragment
                val bundle = bundleOf("REGION_KEY" to selectedRegion!!.name)
                findNavController().navigate(R.id.action_workspace_to_scan, bundle)
            } else {
                Toast.makeText(requireContext(), "Seleziona una zona prima di continuare", Toast.LENGTH_SHORT).show()
            }
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
            // Cambia stile (puoi usare i Drawable creati dalla tua compagna)
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