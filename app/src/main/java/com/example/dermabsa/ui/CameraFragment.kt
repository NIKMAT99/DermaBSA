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
import com.example.dermabsa.R
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Permesso fotocamera negato.", Toast.LENGTH_LONG).show()
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
        val regioneScelta = arguments?.getString("REGION_KEY") ?: "TRUNK_FRONT"

        when (regioneScelta) {
            "HEAD_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_head_f)
            "HEAD_BACK" -> overlayImg.setImageResource(R.drawable.overlay_head_b)
            "TRUNK_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_petto_f)
            "ABDOMEN" -> overlayImg.setImageResource(R.drawable.overlay_addome_f)
            "UPPER_BACK" -> overlayImg.setImageResource(R.drawable.overlay_tronco_b)
            "LOWER_BACK" -> overlayImg.setImageResource(R.drawable.overlay_lower_b)
            "ARM_LEFT_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_arm_fsx)
            "ARM_RIGHT_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_arm_fdx)
            "ARM_LEFT_BACK" -> overlayImg.setImageResource(R.drawable.overlay_arm_bsx)
            "ARM_RIGHT_BACK" -> overlayImg.setImageResource(R.drawable.overlay_arm_bdx)
            "LEG_LEFT_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_leg_fsx)
            "LEG_RIGHT_FRONT" -> overlayImg.setImageResource(R.drawable.overlay_leg_fdx)
            "LEG_LEFT_BACK" -> overlayImg.setImageResource(R.drawable.overlay_leg_bsx)
            "LEG_RIGHT_BACK" -> overlayImg.setImageResource(R.drawable.overlay_leg_bdx)
            "GENITALS" -> overlayImg.setImageResource(R.drawable.overlay_gen)
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
                    val bundle = Bundle().apply {
                        putString("photoUri", savedUri.toString())
                    }
                    findNavController().navigate(R.id.action_camera_to_confirm, bundle)
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }
}