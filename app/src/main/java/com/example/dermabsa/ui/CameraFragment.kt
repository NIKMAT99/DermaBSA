package com.example.dermabsa.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(R.layout.fragment_camera) {

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView

    // 1. GESTIONE DEL POP-UP DEI PERMESSI
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera() // Permesso accordato, accendiamo i motori!
        } else {
            Toast.makeText(requireContext(), "Permesso fotocamera negato. Impossibile procedere.", Toast.LENGTH_LONG).show()
            findNavController().popBackStack() // Torna indietro
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewFinder = view.findViewById(R.id.view_finder)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // 2. IMPOSTA L'OVERLAY DINAMICO IN BASE ALLA ZONA SCELTA
        impostaOverlayDinamico(view)

        // 3. CONTROLLA E CHIEDI IL PERMESSO FOTOCAMERA
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        // 4. AZIONI DEI BOTTONI
        view.findViewById<ImageButton>(R.id.btn_close_camera).setOnClickListener {
            findNavController().popBackStack() // Tasto X: chiude e torna indietro
        }

        view.findViewById<MaterialCardView>(R.id.btn_capture_photo).setOnClickListener {
            scattaFoto() // Tasto grande bianco: Clic!
        }
    }

    private fun impostaOverlayDinamico(view: View) {
        val overlayImg = view.findViewById<ImageView>(R.id.iv_camera_guide_overlay)

        // Recuperiamo il nome della zona passata dal WorkspaceFragment
        // Se non trova nulla, usa "Tronco" come default per sicurezza
        val regioneScelta = arguments?.getString("selectedRegion") ?: "Tronco"

        // Cambiamo l'immagine in base al nome.
        // ATTENZIONE: Assicurati che i nomi (R.drawable...) corrispondano ai file PNG che hai salvato!
        when (regioneScelta) {
            "Testa" -> overlayImg.setImageResource(R.drawable.overlay_testa) // Sostituisci con il nome reale del tuo file
            "Tronco Anteriore", "Tronco" -> overlayImg.setImageResource(R.drawable.overlay_tronco) // Sostituisci con il tuo file
            "Gamba Sinistra (Fronte)" -> overlayImg.setImageResource(R.drawable.overlay_gamba) // Ecc...
            // Aggiungi qui gli altri casi man mano che crei i ritagli!
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Imposta l'anteprima sullo schermo
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // Prepara il "motore" per scattare la foto
            imageCapture = ImageCapture.Builder().build()

            // Scegli la fotocamera posteriore di default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                // Collega fotocamera, ciclo di vita del fragment, anteprima e scatto
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("DermaBSA", "Uso della fotocamera fallito", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun scattaFoto() {
        val imageCapture = imageCapture ?: return

        // Crea un file temporaneo sicuro dove salvare la foto
        val photoFile = File(
            requireContext().externalCacheDir,
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALY).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(requireContext(), "Errore salvataggio foto", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)

                    // FOTO SALVATA! Ora passiamo alla schermata di CONFERMA passandogli l'URI della foto
                    val bundle = Bundle().apply {
                        putString("photoUri", savedUri.toString())
                    }
                    findNavController().navigate(R.id.action_cameraFragment_to_photoConfirmFragment, bundle)
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown() // Chiudiamo il processo in background quando usciamo
    }
}