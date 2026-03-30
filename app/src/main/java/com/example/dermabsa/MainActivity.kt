package com.example.dermabsa

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Import dei nostri moduli (assicurati che i package corrispondano ai tuoi)
import com.example.dermabsa.model.BodyRegion
import com.example.dermabsa.utils.AILesionDetector
import com.example.dermabsa.ui.AlignmentView

class MainActivity : AppCompatActivity() {

    // --- VARIABILI GLOBALI DELLA CLASSE ---
    private lateinit var aiLesionDetector: AILesionDetector
    private var selectedRegion: BodyRegion = BodyRegion.HEAD // Valore di default

    // Launcher per aprire la galleria e caricare la foto
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { imageUri ->
            try {
                val patientBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(contentResolver, imageUri)
                    ImageDecoder.decodeBitmap(source).copy(android.graphics.Bitmap.Config.ARGB_8888, true)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                }

                // Carica l'immagine della mappa (la compagna deve chiamarla mappa_corpo in res/drawable)
                val mapBitmap = BitmapFactory.decodeResource(resources, R.drawable.mappa_corpo)

                val alignmentView = findViewById<AlignmentView>(R.id.alignment_view)
                alignmentView.setImages(mapBitmap, patientBitmap)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Errore nel caricamento dell'immagine", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- METODO ONCREATE (Viene eseguito all'avvio dell'app) ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main) // Qui carichiamo la grafica della tua compagna

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. INIZIALIZZAZIONE DEL MOTORE IA
        aiLesionDetector = AILesionDetector(this)

        // 2. COLLEGAMENTO ELEMENTI GRAFICI (UI Binding)
        val alignmentView = findViewById<AlignmentView>(R.id.alignment_view)
        val calculateButton = findViewById<Button>(R.id.btn_calculate)
        val loadPhotoButton = findViewById<Button>(R.id.btn_load_photo)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        val textViewResult = findViewById<TextView>(R.id.tv_result)

        // Bottoni della mappa anatomica (Esempi)
        val btnHead = findViewById<View>(R.id.btn_region_head)
        val btnArmLeft = findViewById<View>(R.id.btn_region_arm_left)
        val btnArmRight = findViewById<View>(R.id.btn_region_arm_right)
        val btnTrunkFront = findViewById<View>(R.id.btn_region_trunk_front)
        val btnTrunkBack = findViewById<View>(R.id.btn_region_trunk_back)
        val btnLegLeft = findViewById<View>(R.id.btn_region_leg_left)
        val btnLegRight = findViewById<View>(R.id.btn_region_leg_right)

        // 3. IMPOSTAZIONE DEI LISTENER (Cosa succede quando l'utente clicca)

        // Click su "Carica Foto"
        loadPhotoButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Click sulle parti del corpo nella mappa
        btnHead.setOnClickListener {
            selectedRegion = BodyRegion.HEAD
            Toast.makeText(this, "Selezionata: Testa (9%)", Toast.LENGTH_SHORT).show()
        }
        btnArmLeft.setOnClickListener {
            selectedRegion = BodyRegion.ARM_LEFT
            Toast.makeText(this, "Selezionata: Bracico sinitro (9%)", Toast.LENGTH_SHORT).show()
        }
        btnArmRight.setOnClickListener {
            selectedRegion = BodyRegion.ARM_RIGHT
            Toast.makeText(this, "Selezionata: Braccio destro (9%)", Toast.LENGTH_SHORT).show()
        }
        btnTrunkFront.setOnClickListener {
            selectedRegion = BodyRegion.TRUNK_FRONT
            Toast.makeText(this, "Selezionata: Tronco anteriore (9%)", Toast.LENGTH_SHORT).show()
        }
        btnHead.setOnClickListener {
            selectedRegion = BodyRegion.HEAD
            Toast.makeText(this, "Selezionata: Testa (9%)", Toast.LENGTH_SHORT).show()
        }
        btnHead.setOnClickListener {
            selectedRegion = BodyRegion.HEAD
            Toast.makeText(this, "Selezionata: Testa (9%)", Toast.LENGTH_SHORT).show()
        }

        btnTrunkBack.setOnClickListener {
            selectedRegion = BodyRegion.TRUNK_BACK
            Toast.makeText(this, "Selezionato: Tronco Posteriore (13%)", Toast.LENGTH_SHORT).show()
        }

        // Click su "Calcola Percentuale BSA"
        calculateButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            calculateButton.isEnabled = false
            textViewResult.text = "Analisi in corso..."

            lifecycleScope.launch {
                val result = withContext(Dispatchers.Default) {
                    val finalImage = alignmentView.getAlignedBitmap()
                    val regionTotalPixels = finalImage.width * finalImage.height

                    // Passiamo al calcolatore la foto, la regione selezionata dinamicamente e l'area totale
                    aiLesionDetector.analyzeImageAndCalculateBsa(
                        alignedImage = finalImage,
                        region = selectedRegion,
                        regionTotalPixels = regionTotalPixels
                    )
                }

                // Ritorno alla UI per mostrare il risultato
                progressBar.visibility = View.GONE
                calculateButton.isEnabled = true
                val formattedBsa = String.format("%.2f", result.finalInvolvedPercentage)
                textViewResult.text = "Superficie Corporea Coinvolta: $formattedBsa%"
            }
        }
    }

    // --- METODO ONDESTROY (Viene eseguito quando l'app si chiude) ---
    override fun onDestroy() {
        super.onDestroy()
        if (::aiLesionDetector.isInitialized) {
            aiLesionDetector.close() // Liberiamo la memoria!
        }
    }
}