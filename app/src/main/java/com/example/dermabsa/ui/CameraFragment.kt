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
import androidx.camera.core.AspectRatio
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
        val btnFlash = view.findViewById<ImageButton>(R.id.btn_flash)
        var isFlashOn = false // Di base il flash è spento

        btnFlash.setOnClickListener {
            // Invertiamo lo stato: se era spento si accende, se era acceso si spegne
            isFlashOn = !isFlashOn

            if (isFlashOn) {
                // Cambiamo l'icona
                btnFlash.setImageResource(R.drawable.ic_flash_on)
                // Diciamo a CameraX di usare il flash al momento dello scatto
                imageCapture?.flashMode = androidx.camera.core.ImageCapture.FLASH_MODE_ON
            } else {
                // Rimettiamo l'icona del flash spento
                btnFlash.setImageResource(R.drawable.ic_flash_off)
                // Spegniamo il flash in CameraX
                imageCapture?.flashMode = androidx.camera.core.ImageCapture.FLASH_MODE_OFF
            }
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

        // 1. Leggiamo la regione passata come testo (String)
        val regioneScelta = arguments?.getString("REGION_KEY") ?: "CHEST"

        // 2. Convertiamo il testo nel tipo 'BodyRegion' che il ViewModel si aspetta!
        try {
            viewModel.selectedRegion.value = BodyRegion.valueOf(regioneScelta)
        } catch (e: Exception) {
            // Se per caso c'è un errore di battitura, mettiamo il tronco di default per non far crashare l'app
            viewModel.selectedRegion.value = BodyRegion.CHEST
        }

        // 3. Funzione per convertire i DP in Pixel (necessario per setPadding da codice)
        val scale = resources.displayMetrics.density
        fun dpToPx(dp: Int): Int = (dp * scale + 0.5f).toInt()

        var resId = R.drawable.overlay_petto_f
        var paddingInDp = 50 // Padding standard

        // 4. Decidiamo l'immagine E il padding in base alle proporzioni anatomiche
        when (regioneScelta) {
            // TESTA E COLLO (Parti medio-piccole, padding grande)
            "HEAD_FRONT" -> { resId = R.drawable.overlay_head_f; paddingInDp = 20 }
            "HEAD_BACK" -> { resId = R.drawable.overlay_head_b; paddingInDp = 20 }
            "NECK_FRONT" -> { resId = R.drawable.overlay_neck_f; paddingInDp = 20 }
            "NECK_BACK" -> { resId = R.drawable.overlay_neck_b; paddingInDp = 20 }

            // TRONCO E ADDOME (Parti larghe, padding medio)
            "CHEST" -> { resId = R.drawable.overlay_petto_f; paddingInDp = 10 }
            "ABDOMEN" -> { resId = R.drawable.overlay_addome_f; paddingInDp = 10 }
            "UPPER_BACK" -> { resId = R.drawable.overlay_tronco_b; paddingInDp = 10 }
            "LOWER_BACK" -> { resId = R.drawable.overlay_lower_b; paddingInDp = 10 }

            // BRACCIA E AVAMBRACCIA (Parti lunghe, padding piccolo per farle stendere)
            "UPPER_ARM_LEFT_FRONT" -> { resId = R.drawable.overlay_upper_arm_fsx; paddingInDp = 10 }
            "UPPER_ARM_RIGHT_FRONT" -> { resId = R.drawable.overlay_upper_arm_fdx; paddingInDp = 10 }
            "UPPER_ARM_LEFT_BACK" -> { resId = R.drawable.overlay_upper_arm_bsx; paddingInDp = 10 }
            "UPPER_ARM_RIGHT_BACK" -> { resId = R.drawable.overlay_upper_arm_bdx; paddingInDp = 10 }
            "FOREARM_LEFT_FRONT" -> { resId = R.drawable.overlay_forearm_fsx; paddingInDp = 10 }
            "FOREARM_RIGHT_FRONT" -> { resId = R.drawable.overlay_forearm_fdx; paddingInDp = 10 }
            "FOREARM_LEFT_BACK" -> { resId = R.drawable.overlay_forearm_bsx; paddingInDp = 10 }
            "FOREARM_RIGHT_BACK" -> { resId = R.drawable.overlay_forearm_bdx; paddingInDp = 10 }

            // MANI (Parti molto piccole, padding molto grande)
            "HAND_LEFT_FRONT" -> { resId = R.drawable.overlay_hand_fsx; paddingInDp = 20 }
            "HAND_RIGHT_FRONT" -> { resId = R.drawable.overlay_hand_fdx; paddingInDp = 20 }
            "HAND_LEFT_BACK" -> { resId = R.drawable.overlay_hand_bsx; paddingInDp = 20 }
            "HAND_RIGHT_BACK" -> { resId = R.drawable.overlay_hand_bdx; paddingInDp = 20 }

            // GENITALI E GLUTEI
            "GENITALS" -> { resId = R.drawable.overlay_gen; paddingInDp = 10 }
            "BUTTOCK_LEFT" -> { resId = R.drawable.overlay_buttock_sx; paddingInDp = 20 }
            "BUTTOCK_RIGHT" -> { resId = R.drawable.overlay_buttock_dx; paddingInDp = 20 }

            // COSCE E GAMBE (Parti lunghe, padding piccolo)
            "THIGH_LEFT_FRONT" -> { resId = R.drawable.overlay_thigh_fsx; paddingInDp = 20 }
            "THIGH_RIGHT_FRONT" -> { resId = R.drawable.overlay_thigh_fdx; paddingInDp = 20 }
            "THIGH_LEFT_BACK" -> { resId = R.drawable.overlay_thigh_bsx; paddingInDp = 20 }
            "THIGH_RIGHT_BACK" -> { resId = R.drawable.overlay_thigh_bdx; paddingInDp = 20 }
            "LOWER_LEG_LEFT_FRONT" -> { resId = R.drawable.overlay_leg_fsx; paddingInDp = 10 }
            "LOWER_LEG_RIGHT_FRONT" -> { resId = R.drawable.overlay_leg_fdx; paddingInDp = 10 }
            "LOWER_LEG_LEFT_BACK" -> { resId = R.drawable.overlay_leg_bsx; paddingInDp = 10 }
            "LOWER_LEG_RIGHT_BACK" -> { resId = R.drawable.overlay_leg_bdx; paddingInDp = 10 }

            // PIEDI (Parti molto piccole, padding molto grande)
            "FOOT_LEFT_FRONT" -> { resId = R.drawable.overlay_foot_fsx; paddingInDp = 20 }
            "FOOT_RIGHT_FRONT" -> { resId = R.drawable.overlay_foot_fdx; paddingInDp = 20 }
            "FOOT_LEFT_BACK" -> { resId = R.drawable.overlay_foot_bsx; paddingInDp = 20 }
            "FOOT_RIGHT_BACK" -> { resId = R.drawable.overlay_foot_bdx; paddingInDp = 20 }

            else -> { resId = R.drawable.overlay_petto_f; paddingInDp = 50 }
        }

        // 5. Applichiamo l'immagine e i margini calcolati
        overlayImg.setImageResource(resId)
        val p = dpToPx(paddingInDp)
        overlayImg.setPadding(p, p, p, p)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // 1. Diciamo all'anteprima di usare il formato 16:9
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build().also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // 2. MAGIA: Diciamo anche allo SCATTO di usare i 16:9!
            imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build()

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