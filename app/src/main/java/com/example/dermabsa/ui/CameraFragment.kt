package com.example.dermabsa.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dermabsa.R
import com.example.dermabsa.model.BodyRegion
import com.google.android.material.card.MaterialCardView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.example.dermabsa.ui.MainViewModel
import com.google.android.material.snackbar.Snackbar

class CameraFragment : Fragment(R.layout.fragment_camera) {

    private val viewModel: MainViewModel by activityViewModels()
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            // Troviamo la root view per far sopravvivere la Snackbar alla chiusura della pagina
            val rootView = requireActivity().findViewById<View>(android.R.id.content)

            Snackbar.make(rootView, "Permesso fotocamera negato.", Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.derma_text_dark))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                .show()

            findNavController().popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewFinder = view.findViewById(R.id.view_finder)
        cameraExecutor = Executors.newSingleThreadExecutor()

        impostaOverlayDinamico(view)

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        view.findViewById<View>(R.id.btn_capture_photo).setOnClickListener {
            scattaFoto()
        }
    }

    private fun impostaOverlayDinamico(view: View) {
        val overlayImg = view.findViewById<ImageView>(R.id.iv_camera_guide_overlay)

        // 1. Leggiamo la regione passata
        // 1. Leggiamo la regione passata come testo (String)
        val regioneScelta = arguments?.getString("REGION_KEY") ?: "TRUNK_FRONT"

        // 2. Convertiamo il testo nel tipo 'BodyRegion' che il ViewModel si aspetta!
        try {
            viewModel.selectedRegion.value = BodyRegion.valueOf(regioneScelta)
        } catch (e: Exception) {
            // Se per caso c'è un errore di battitura, mettiamo il tronco di default per non far crashare l'app
            viewModel.selectedRegion.value = BodyRegion.CHEST
        }

        when (regioneScelta) {
            // TESTA E COLLO
            "HEAD_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_head_f)
            "HEAD_BACK" -> overlayImg.setImageResource(R.drawable.overlay_head_b)
            "NECK_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_neck_f)
            "NECK_BACK" -> overlayImg.setImageResource(R.drawable.overlay_neck_b)

            // TRONCO
            "CHEST" -> overlayImg.setImageResource(R.drawable.overlay_petto_f)
            "ABDOMEN" -> overlayImg.setImageResource(R.drawable.overlay_addome_f)
            "UPPER_BACK" -> overlayImg.setImageResource(R.drawable.overlay_tronco_b)
            "LOWER_BACK" -> overlayImg.setImageResource(R.drawable.overlay_lower_b)

            // BRACCIA SUPERIORI
            "UPPER_ARM_LEFT_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_upper_arm_fsx)
            "UPPER_ARM_RIGHT_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_upper_arm_fdx)
            "UPPER_ARM_LEFT_BACK" -> overlayImg.setImageResource(R.drawable.overlay_upper_arm_bsx)
            "UPPER_ARM_RIGHT_BACK" -> overlayImg.setImageResource(R.drawable.overlay_upper_arm_bdx)

            // AVAMBRACCIA
            "FOREARM_LEFT_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_forearm_fsx)
            "FOREARM_RIGHT_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_forearm_fdx)
            "FOREARM_LEFT_BACK" -> overlayImg.setImageResource(R.drawable.overlay_forearm_bsx)
            "FOREARM_RIGHT_BACK" -> overlayImg.setImageResource(R.drawable.overlay_forearm_bdx)

            // MANI
            "HAND_LEFT_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_hand_fsx)
            "HAND_RIGHT_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_hand_fdx)
            "HAND_LEFT_BACK" -> overlayImg.setImageResource(R.drawable.overlay_hand_bsx)
            "HAND_RIGHT_BACK" -> overlayImg.setImageResource(R.drawable.overlay_hand_bdx)

            // GENITALI E GLUTEI
            "GENITALS" -> overlayImg.setImageResource(R.drawable.overlay_gen)
            "BUTTOCK_LEFT" -> overlayImg.setImageResource(R.drawable.overlay_buttock_sx)
            "BUTTOCK_RIGHT" -> overlayImg.setImageResource(R.drawable.overlay_buttock_dx)

            // COSCE
            "THIGH_LEFT_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_thigh_fsx)
            "THIGH_RIGHT_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_thigh_fdx)
            "THIGH_LEFT_BACK" -> overlayImg.setImageResource(R.drawable.overlay_thigh_bsx)
            "THIGH_RIGHT_BACK" -> overlayImg.setImageResource(R.drawable.overlay_thigh_bdx)

            // GAMBE (STINCHI/POLPACCI)
            "LOWER_LEG_LEFT_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_leg_fsx)
            "LOWER_LEG_RIGHT_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_leg_fdx)
            "LOWER_LEG_LEFT_BACK" -> overlayImg.setImageResource(R.drawable.overlay_leg_bsx)
            "LOWER_LEG_RIGHT_BACK" -> overlayImg.setImageResource(R.drawable.overlay_leg_bdx)

            // PIEDI
            "FOOT_LEFT_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_foot_fsx)
            "FOOT_RIGHT_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_foot_fdx)
            "FOOT_LEFT_BACK" -> overlayImg.setImageResource(R.drawable.overlay_foot_bsx)
            "FOOT_RIGHT_BACK" -> overlayImg.setImageResource(R.drawable.overlay_foot_bdx)

            else -> overlayImg.setImageResource(R.drawable.overlay_gen)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("DermaBSA", "Uso della fotocamera fallito", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun scattaFoto() {
        val imageCapture = imageCapture ?: return

        // Uso 'cacheDir' invece di 'externalCacheDir' per prevenire crash su alcuni dispositivi
        val photoFile = File(
            requireContext().cacheDir,
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALY).format(System.currentTimeMillis()) + ".jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Snackbar.make(requireView(), "Errore durante il salvataggio della foto", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.derma_text_dark))
                        .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        .show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)

                    try {
                        // 1. Convertiamo l'Uri appena scattato in Bitmap
                        val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source = ImageDecoder.createSource(requireContext().contentResolver, savedUri)
                            ImageDecoder.decodeBitmap(source)
                        } else {
                            @Suppress("DEPRECATION")
                            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, savedUri)
                        }

                        // 2. Copiamo in ARGB_8888 e lo mettiamo nel ViewModel (esattamente come in ScanFragment!)
                        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                        viewModel.patientPhoto.value = mutableBitmap

                        // 3. Prepariamo il Bundle e proseguiamo
                        val bundle = Bundle().apply {
                            putString("photoUri", savedUri.toString())
                            // Passiamo avanti anche la REGION_KEY per i frammenti successivi se serve
                            putString("REGION_KEY", arguments?.getString("REGION_KEY"))
                        }
                        findNavController().navigate(R.id.action_camera_to_confirm, bundle)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Snackbar.make(requireView(), "Errore elaborazione immagine", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.derma_text_dark))
                            .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                            .show()
                    }
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }
}