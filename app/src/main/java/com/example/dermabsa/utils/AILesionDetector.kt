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
    fun analyzeImageAndCalculateBsa(alignedImage: Bitmap, region: BodyRegion, regionTotalPixels: Int): BsaResult {
        val tflite = interpreter ?: throw IllegalStateException("Il modello IA non è stato caricato correttamente.")

        // 1. Rimpiccioliamo a 256x256
        val resizedBitmap = Bitmap.createScaledBitmap(alignedImage, 256, 256, true)

        // --- LA SOLUZIONE ALL'ERRORE: TRADUZIONE IN FLOAT32 (0.0 - 1.0) ---
        // Creiamo una scatola di byte esatta per 256x256 pixel * 3 canali (RGB) * 4 byte (Float) = 786432 byte!
        val byteBuffer = ByteBuffer.allocateDirect(4 * 256 * 256 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        // Estraiamo i pixel dall'immagine
        val intValues = IntArray(256 * 256)
        resizedBitmap.getPixels(intValues, 0, 256, 0, 0, 256, 256)

        var pixel = 0
        for (i in 0 until 256) {
            for (j in 0 until 256) {
                val valPixel = intValues[pixel++]
                // Separiamo i colori (Rosso, Verde, Blu) e dividiamo per 255.0 per avere valori da 0.0 a 1.0
                byteBuffer.putFloat(((valPixel shr 16) and 0xFF) / 255.0f) // R
                byteBuffer.putFloat(((valPixel shr 8) and 0xFF) / 255.0f)  // G
                byteBuffer.putFloat((valPixel and 0xFF) / 255.0f)          // B
            }
        }
        // -----------------------------------------------------------------

        // 2. Prepariamo la maschera per ricevere i risultati (formato 1x256x256x1)
        val outputMask = Array(1) { Array(256) { Array(256) { FloatArray(1) } } }

        // 3. ESECUZIONE DELL'IA (Passiamo il byteBuffer invece del tensorImage)
        tflite.run(byteBuffer, outputMask)

        // 4. Analisi dei risultati
        var lesionPixelsInMask = 0
        val totalPixelsInMask = 256 * 256

        // Contiamo quanti pixel l'IA ha classificato come psoriasi
        for (x in 0 until 256) {
            for (y in 0 until 256) {
                // > 0.5f significa che l'IA è sicura al 50% o più che si tratti di lesione
                if (outputMask[0][x][y][0] > 0.5f) {
                    lesionPixelsInMask++
                }
            }
        }

        // 5. Proporzione rispetto alla foto originale
        val ratio = lesionPixelsInMask.toDouble() / totalPixelsInMask.toDouble()
        val estimatedLesionPixelsInOriginal = (ratio * regionTotalPixels).toInt()

        // 6. Calcolo del risultato BSA tramite il tuo calcolatore
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