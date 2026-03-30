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
    fun analyzeImageAndCalculateBsa(alignedImage: Bitmap, region: BodyRegion, regionTotalPixels: Int): BsaResult {
        // 1. Preparazione: Le IA di solito richiedono immagini rimpicciolite (es. 256x256)
        val resizedBitmap = Bitmap.createScaledBitmap(alignedImage, 256, 256, true)
        val tensorImage = TensorImage.fromBitmap(resizedBitmap)

        // 2. Output: Prepariamo un array vuoto per ricevere la "maschera" dalla IA.
        // Immaginiamo che il modello restituisca una mappa 256x256 dove ogni pixel ha un valore da 0.0 a 1.0 (probabilità di psoriasi)
        val outputMask = Array(1) { Array(256) { FloatArray(256) } }

        // 3. ESECUZIONE: Facciamo girare il modello!
        interpreter?.run(tensorImage.buffer, outputMask)

        // 4. Analisi del Risultato (Post-processing)
        var lesionPixelsInMask = 0
        var totalPixelsInMask = 256 * 256

        for (x in 0 until 256) {
            for (y in 0 until 256) {
                // Se la probabilità è maggiore del 50%, consideriamo il pixel come "lesione"
                if (outputMask[0][x][y] > 0.5f) {
                    lesionPixelsInMask++
                }
            }
        }

        // 5. Proporzione: Riportiamo i pixel della maschera rimpicciolita alla dimensione originale della foto
        val ratio = lesionPixelsInMask.toDouble() / totalPixelsInMask.toDouble()
        val estimatedLesionPixelsInOriginal = (ratio * regionTotalPixels).toInt()

        // 6. Calcolo Clinico Finale tramite il nostro BsaCalculator
        return BsaCalculator.calculateLesionBsa(
            region = region,
            lesionAreaPixels = estimatedLesionPixelsInOriginal,
            regionTotalAreaPixels = regionTotalPixels
        )
    }

    // Ricordiamoci di liberare la memoria quando non serve più!
    fun close() {
        interpreter?.close()
    }
}