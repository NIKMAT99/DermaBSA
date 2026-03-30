package com.example.dermabsa.model

data class BsaResult(
    val region: BodyRegion,
    val lesionAreaPixels: Int,
    val regionTotalAreaPixels: Int,
    val finalInvolvedPercentage: Double
)