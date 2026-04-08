package com.example.dermabsa.model

import android.graphics.Bitmap

data class BsaResult(
    val region: BodyRegion,
    val lesionAreaPixels: Int,
    val regionTotalAreaPixels: Int,
    val finalInvolvedPercentage: Double,
    val processedImage: Bitmap? = null
)