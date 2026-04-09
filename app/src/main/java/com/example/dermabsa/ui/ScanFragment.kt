package com.example.dermabsa.ui

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dermabsa.R

class ScanFragment : Fragment(R.layout.fragment_scan) {

    // Inizializza il ViewModel a livello di Activity
    private val viewModel: MainViewModel by activityViewModels()

    // Gestore per l'apertura della galleria e la selezione dell'immagine
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // Conversione da URI a Bitmap in base alla versione di Android
                val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                }

                // Copiamo il Bitmap in formato ARGB_8888 (formato standard richiesto spesso da TensorFlow)
                val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

                // Salviamo l'immagine nel ViewModel
                viewModel.patientPhoto.value = mutableBitmap

                // Procediamo al frammento di conferma
                findNavController().navigate(R.id.action_scan_to_confirm)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Associazione degli ID del tuo fragment_scan.xml
        val btnGallery = view.findViewById<LinearLayout>(R.id.btn_pick_gallery)
        val btnCamera = view.findViewById<LinearLayout>(R.id.btn_take_photo)

        // Navigazione verso il frammento della fotocamera proprietaria
        btnCamera.setOnClickListener {
            findNavController().navigate(R.id.action_scan_to_camera)
        }

        // Avvio della selezione dalla galleria limitata ai file immagine
        btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }
}