package com.example.dermabsa.utils

import android.content.Context
import android.graphics.Bitmap
import com.example.dermabsa.model.BodyRegion
import com.example.dermabsa.model.BsaResult
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Gestisce l'Intelligenza Artificiale locale per trovare le lesioni da psoriasi.
 */
class AILesionDetector(context: Context) {

    private var interpreter: Interpreter? = null

    init {
        // Quando la classe viene creata, carichiamo il modello in memoria
        val modelBuffer = loadModelFile(context, "psoriasis_segmentation.tflite")
        interpreter = Interpreter(modelBuffer)
    }

    /**
     * Funzione di utilità per caricare il file .tflite dagli assets.
     */
    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    /**
     * Il cuore dell'IA: analizza la foto e restituisce il calcolo BSA.
     */
    fun analyzeImageAndCalculateBsa(alignedImage: Bitmap, region: BodyRegion, regionTotalPixels: Int): Pair<BsaResult, Bitmap> {
        val tflite = interpreter ?: throw IllegalStateException("Il modello IA non è stato caricato correttamente.")
        // 1. Rimpiccioliamo a 256x256
        val resizedBitmap = Bitmap.createScaledBitmap(alignedImage, 256, 256, true)

        val resultBitmap = resizedBitmap.copy(Bitmap.Config.ARGB_8888, true)

        val byteBuffer = ByteBuffer.allocateDirect(4 * 256 * 256 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        // Estraiamo i pixel dall'immagine
        val intValues = IntArray(256 * 256)
        resizedBitmap.getPixels(intValues, 0, 256, 0, 0, 256, 256)

        var pixel = 0
        for (i in 0 until 256) {
            for (j in 0 until 256) {
                val valPixel = intValues[pixel++]

                val r = ((valPixel shr 16) and 0xFF) / 255.0f
                val g = ((valPixel shr 8) and 0xFF) / 255.0f
                val b = (valPixel and 0xFF) / 255.0f

                byteBuffer.putFloat(b) // Prima il Blu
                byteBuffer.putFloat(g) // Poi il Verde
                byteBuffer.putFloat(r) // Infine il Rosso
            }
        }
        byteBuffer.rewind()

        // 2. Prepariamo la maschera per ricevere i risultati (formato 1x256x256x1)
        val outputMask = Array(1) { Array(256) { Array(256) { FloatArray(1) } } }

        // 3. ESECUZIONE DELL'IA (Passiamo il byteBuffer invece del tensorImage)
        tflite.run(byteBuffer, outputMask)

        // 4. Analisi dei risultati
        var lesionPixelsInMask = 0
        // --- IL FIX MAGICO PER SFONDO E ZOOM (AGGIORNATO LUND-BROWDER) ---
        val bodyCoveragePercentage = when (region) {
            BodyRegion.CHEST, BodyRegion.UPPER_BACK -> 0.70
            BodyRegion.ABDOMEN, BodyRegion.LOWER_BACK -> 0.55

            BodyRegion.THIGH_LEFT_FRONT, BodyRegion.THIGH_RIGHT_FRONT,
            BodyRegion.THIGH_LEFT_BACK, BodyRegion.THIGH_RIGHT_BACK,
            BodyRegion.LOWER_LEG_LEFT_FRONT, BodyRegion.LOWER_LEG_RIGHT_FRONT,
            BodyRegion.LOWER_LEG_LEFT_BACK, BodyRegion.LOWER_LEG_RIGHT_BACK,
            BodyRegion.BUTTOCK_LEFT, BodyRegion.BUTTOCK_RIGHT -> 0.40 // Gambe e glutei

            BodyRegion.HEAD_FRONT, BodyRegion.HEAD_BACK -> 0.35 // Testa

            BodyRegion.UPPER_ARM_LEFT_FRONT, BodyRegion.UPPER_ARM_RIGHT_FRONT,
            BodyRegion.UPPER_ARM_LEFT_BACK, BodyRegion.UPPER_ARM_RIGHT_BACK,
            BodyRegion.FOREARM_LEFT_FRONT, BodyRegion.FOREARM_RIGHT_FRONT,
            BodyRegion.FOREARM_LEFT_BACK, BodyRegion.FOREARM_RIGHT_BACK -> 0.30 // Braccia sottili

            BodyRegion.HAND_LEFT_FRONT, BodyRegion.HAND_RIGHT_FRONT,
            BodyRegion.HAND_LEFT_BACK, BodyRegion.HAND_RIGHT_BACK,
            BodyRegion.FOOT_LEFT_FRONT, BodyRegion.FOOT_RIGHT_FRONT,
            BodyRegion.FOOT_LEFT_BACK, BodyRegion.FOOT_RIGHT_BACK -> 0.40 // Mani e piedi (zoomati)

            BodyRegion.NECK_FRONT, BodyRegion.NECK_BACK, BodyRegion.GENITALS -> 0.20 // Zone piccole
        }

        val bodyPixelsInMask = (256 * 256 * bodyCoveragePercentage).toInt()

        for (y in 0 until 256) {
            for (x in 0 until 256) {
                // L'IA ci dice che questo pixel è psoriasi!
                if (outputMask[0][y][x][0] > 0.5f) {
                    lesionPixelsInMask++

                    // --- EFFETTO EVIDENZIATORE ROSSO ---
                    // Prendiamo il colore originale della pelle
                    val originalColor = resultBitmap.getPixel(x, y)
                    val oldR = android.graphics.Color.red(originalColor)
                    val oldG = android.graphics.Color.green(originalColor)
                    val oldB = android.graphics.Color.blue(originalColor)

                    // Creiamo un mix: mescoliamo il rosso puro (255) con il colore originale
                    val newR = (255 + oldR) / 2
                    val newG = oldG / 2
                    val newB = oldB / 2

                    // Dipingiamo il nuovo pixel sulla foto!
                    resultBitmap.setPixel(x, y, android.graphics.Color.rgb(newR, newG, newB))
                }
            }
        }
        android.util.Log.d("DermaBSA_AI", "Pixel Psoriasi Trovati: $lesionPixelsInMask su $bodyPixelsInMask (Pixel Corpo Stimati)")
        // 5. Calcolo BSA
        val ratio = (lesionPixelsInMask.toDouble() / bodyPixelsInMask.toDouble()).coerceAtMost(1.0)

        val estimatedLesionPixelsInOriginal = (ratio * regionTotalPixels).toInt()

        val result = BsaCalculator.calculateLesionBsa(
            region = region,
            lesionAreaPixels = estimatedLesionPixelsInOriginal,
            regionTotalAreaPixels = regionTotalPixels
        )

        // 6. RESTITUIAMO ENTRAMBI (Risultato matematico e Foto colorata)
        return Pair(result, resultBitmap)
    }

    // Ricordiamoci di liberare la memoria quando non serve più!
    fun close() {
        interpreter?.close()
    }
}