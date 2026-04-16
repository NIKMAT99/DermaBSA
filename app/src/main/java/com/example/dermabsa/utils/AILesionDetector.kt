package com.example.dermabsa.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.dermabsa.model.BodyRegion
import com.example.dermabsa.model.BsaResult
import com.example.dermabsa.R
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Gestisce l'Intelligenza Artificiale locale per trovare le lesioni da psoriasi.
 */
class AILesionDetector(private val context: Context) { // <-- IMPORTANTE: aggiunto 'private val' qui

    private var interpreter: Interpreter? = null

    init {
        val modelBuffer = loadModelFile(context, "psoriasis_segmentation.tflite")
        interpreter = Interpreter(modelBuffer)
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    fun analyzeImageAndCalculateBsa(alignedImage: Bitmap, region: BodyRegion, regionTotalPixels: Int): Pair<BsaResult, Bitmap> {
        val tflite = interpreter ?: throw IllegalStateException("Il modello IA non è stato caricato.")

        // 1. Prepariamo la foto principale
        val resizedBitmap = Bitmap.createScaledBitmap(alignedImage, 256, 256, true)
        val resultBitmap = resizedBitmap.copy(Bitmap.Config.ARGB_8888, true)

        // --- 2. CARICHIAMO L'OVERLAY COME MASCHERA DI RITAGLIO ---
        val overlayResId = when (region) {
            BodyRegion.HEAD_FRONT -> R.drawable.overlay_head_f
            BodyRegion.HEAD_BACK -> R.drawable.overlay_head_b
            BodyRegion.NECK_FRONT -> R.drawable.overlay_neck_f
            BodyRegion.NECK_BACK -> R.drawable.overlay_neck_b
            BodyRegion.CHEST -> R.drawable.overlay_petto_f
            BodyRegion.ABDOMEN -> R.drawable.overlay_addome_f
            BodyRegion.UPPER_BACK -> R.drawable.overlay_tronco_b
            BodyRegion.LOWER_BACK -> R.drawable.overlay_lower_b
            BodyRegion.UPPER_ARM_LEFT_FRONT -> R.drawable.overlay_upper_arm_fsx
            BodyRegion.UPPER_ARM_RIGHT_FRONT -> R.drawable.overlay_upper_arm_fdx
            BodyRegion.UPPER_ARM_LEFT_BACK -> R.drawable.overlay_upper_arm_bsx
            BodyRegion.UPPER_ARM_RIGHT_BACK -> R.drawable.overlay_upper_arm_bdx
            BodyRegion.FOREARM_LEFT_FRONT -> R.drawable.overlay_forearm_fsx
            BodyRegion.FOREARM_RIGHT_FRONT -> R.drawable.overlay_forearm_fdx
            BodyRegion.FOREARM_LEFT_BACK -> R.drawable.overlay_forearm_bsx
            BodyRegion.FOREARM_RIGHT_BACK -> R.drawable.overlay_forearm_bdx
            BodyRegion.HAND_LEFT_FRONT -> R.drawable.overlay_hand_fsx
            BodyRegion.HAND_RIGHT_FRONT -> R.drawable.overlay_hand_fdx
            BodyRegion.HAND_LEFT_BACK -> R.drawable.overlay_hand_bsx
            BodyRegion.HAND_RIGHT_BACK -> R.drawable.overlay_hand_bdx
            BodyRegion.GENITALS -> R.drawable.overlay_gen
            BodyRegion.BUTTOCK_LEFT -> R.drawable.overlay_buttock_sx
            BodyRegion.BUTTOCK_RIGHT -> R.drawable.overlay_buttock_dx
            BodyRegion.THIGH_LEFT_FRONT -> R.drawable.overlay_thigh_fsx
            BodyRegion.THIGH_RIGHT_FRONT -> R.drawable.overlay_thigh_fdx
            BodyRegion.THIGH_LEFT_BACK -> R.drawable.overlay_thigh_bsx
            BodyRegion.THIGH_RIGHT_BACK -> R.drawable.overlay_thigh_bdx
            BodyRegion.LOWER_LEG_LEFT_FRONT -> R.drawable.overlay_leg_fsx
            BodyRegion.LOWER_LEG_RIGHT_FRONT -> R.drawable.overlay_leg_fdx
            BodyRegion.LOWER_LEG_LEFT_BACK -> R.drawable.overlay_leg_bsx
            BodyRegion.LOWER_LEG_RIGHT_BACK -> R.drawable.overlay_leg_bdx
            BodyRegion.FOOT_LEFT_FRONT -> R.drawable.overlay_foot_fsx
            BodyRegion.FOOT_RIGHT_FRONT -> R.drawable.overlay_foot_fdx
            BodyRegion.FOOT_LEFT_BACK -> R.drawable.overlay_foot_bsx
            BodyRegion.FOOT_RIGHT_BACK -> R.drawable.overlay_foot_bdx
            else -> R.drawable.body_front
        }

        // Rimpiccioliamo l'overlay in modo che si sovrapponga perfettamente alla foto
        val overlayBitmap = BitmapFactory.decodeResource(context.resources, overlayResId)
        val resizedOverlay = Bitmap.createScaledBitmap(overlayBitmap, 256, 256, true)

        // 3. Estraiamo i pixel per l'IA
        val byteBuffer = ByteBuffer.allocateDirect(4 * 256 * 256 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(256 * 256)
        resizedBitmap.getPixels(intValues, 0, 256, 0, 0, 256, 256)

        var pixel = 0
        for (i in 0 until 256) {
            for (j in 0 until 256) {
                val valPixel = intValues[pixel++]
                val r = ((valPixel shr 16) and 0xFF) / 255.0f
                val g = ((valPixel shr 8) and 0xFF) / 255.0f
                val b = (valPixel and 0xFF) / 255.0f

                byteBuffer.putFloat(b)
                byteBuffer.putFloat(g)
                byteBuffer.putFloat(r)
            }
        }
        byteBuffer.rewind()

        val outputMask = Array(1) { Array(256) { Array(256) { FloatArray(4) } } }
        tflite.run(byteBuffer, outputMask)

        // 4. ANALISI INCROCIATA CON LA MASCHERA DELL'OVERLAY
        var lesionPixelsInMask = 0
        var validRegionPixels = 0 // Questo sarà il nostro nuovo denominatore!

        for (y in 0 until 256) {
            for (x in 0 until 256) {

                // Leggiamo la trasparenza (Alpha) dell'overlay in questo esatto punto
                val overlayPixel = resizedOverlay.getPixel(x, y)
                val isInsideOverlay = android.graphics.Color.alpha(overlayPixel) > 10

                val originalColor = resultBitmap.getPixel(x, y)
                val oldR = android.graphics.Color.red(originalColor)
                val oldG = android.graphics.Color.green(originalColor)
                val oldB = android.graphics.Color.blue(originalColor)

                // SE SIAMO DENTRO LA ZONA DEL CORPO SELEZIONATA...
                if (isInsideOverlay) {
                    validRegionPixels++

                    val probSfondo = outputMask[0][y][x][0]
                    val probPsoriasi = outputMask[0][y][x][1]
                    val probPelle = outputMask[0][y][x][2]
                    val probUnghie = outputMask[0][y][x][3]

                    if (probPsoriasi > probSfondo && probPsoriasi > probPelle && probPsoriasi > probUnghie && probPsoriasi > 0.3f) {
                        lesionPixelsInMask++
                        // Evidenziamo la psoriasi in rosso brillante
                        resultBitmap.setPixel(x, y, android.graphics.Color.rgb((255 + oldR) / 2, oldG / 2, oldB / 2))
                    }
                }
                // SE SIAMO FUORI DALLA ZONA (SFONDO)...
                else {
                    // Ignoriamo la malattia e SCURIAMO il pixel!
                    // Così l'utente vedrà visivamente che quest'area è stata esclusa dal calcolo.
                    val dimR = (oldR * 0.4).toInt()
                    val dimG = (oldG * 0.4).toInt()
                    val dimB = (oldB * 0.4).toInt()
                    resultBitmap.setPixel(x, y, android.graphics.Color.rgb(dimR, dimG, dimB))
                }
            }
        }

        android.util.Log.d("DermaBSA_AI", "Area Analizzata (Pixel): $validRegionPixels. Psoriasi Trovata: $lesionPixelsInMask")

        // Sicurezza: evitiamo la divisione per zero
        if (validRegionPixels == 0) validRegionPixels = 1

        val ratio = (lesionPixelsInMask.toDouble() / validRegionPixels.toDouble()).coerceAtMost(1.0)
        val estimatedLesionPixelsInOriginal = (ratio * regionTotalPixels).toInt()

        val result = BsaCalculator.calculateLesionBsa(
            region = region,
            lesionAreaPixels = estimatedLesionPixelsInOriginal,
            regionTotalAreaPixels = regionTotalPixels
        )

        return Pair(result, resultBitmap)
    }

    fun close() {
        interpreter?.close()
    }
}