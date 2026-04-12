package com.example.dermabsa.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

class AlignmentView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mapBitmap: Bitmap? = null
    private var patientPhotoBitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // La matrice che conterrà lo stato attuale delle trasformazioni
    private val photoMatrix = Matrix()

    // Variabili per il trascinamento (Pan)
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = MotionEvent.INVALID_POINTER_ID

    // Gestore per lo Zoom (Scala)
    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            // Applica la scala alla matrice centrando l'azione sul punto in cui l'utente sta "pizzicando"
            photoMatrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
            invalidate() // Ridisegna la vista
            return true
        }
    })

    fun setImages(map: Bitmap?, photo: Bitmap) {
        mapBitmap = map
        patientPhotoBitmap = photo
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Disegna prima la foto del paziente (sotto) applicando la matrice (zoom/spostamento/rotazione)
        patientPhotoBitmap?.let {
            canvas.drawBitmap(it, photoMatrix, paint)
        }

        // 2. Disegna la sagoma (sopra) fissa.
        // Se la sagoma deve occupare tutto lo schermo, usiamo un Rect per scalarla correttamente.
        mapBitmap?.let {
            val src = android.graphics.Rect(0, 0, it.width, it.height)
            val dst = android.graphics.Rect(0, 0, width, height)
            canvas.drawBitmap(it, src, dst, paint)
        }
    }

    /**
     * Intercetta i tocchi sullo schermo per applicare le trasformazioni.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Passa l'evento al detector dello zoom
        scaleDetector.onTouchEvent(event)

        val action = event.actionMasked

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val pointerIndex = event.actionIndex
                lastTouchX = event.getX(pointerIndex)
                lastTouchY = event.getY(pointerIndex)
                activePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(activePointerId)
                if (pointerIndex != -1 && !scaleDetector.isInProgress) {
                    val x = event.getX(pointerIndex)
                    val y = event.getY(pointerIndex)

                    val dx = x - lastTouchX
                    val dy = y - lastTouchY

                    // Applica la traslazione (spostamento) alla matrice
                    photoMatrix.postTranslate(dx, dy)
                    invalidate() // Ridisegna la vista con la nuova posizione

                    lastTouchX = x
                    lastTouchY = y
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId = MotionEvent.INVALID_POINTER_ID
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                if (pointerId == activePointerId) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    lastTouchX = event.getX(newPointerIndex)
                    lastTouchY = event.getY(newPointerIndex)
                    activePointerId = event.getPointerId(newPointerIndex)
                }
            }
        }
        return true // Indica che abbiamo gestito il tocco
    }
    fun getAlignedBitmap(): Bitmap {
        // Crea una nuova Bitmap vuota con le dimensioni della View
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)

        // Forza la View a disegnarsi su questo nuovo Canvas
        this.draw(canvas)

        return resultBitmap
    }

    fun rotate90() {
        // Ruota di 90 gradi prendendo come perno il centro della View
        val centerX = width / 2f
        val centerY = height / 2f
        photoMatrix.postRotate(90f, centerX, centerY)
        invalidate() // Ridisegna
    }
}

