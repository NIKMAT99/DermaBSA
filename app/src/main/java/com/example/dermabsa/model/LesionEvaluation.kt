package com.example.dermabsa.model

data class LesionEvaluation(
    val region: AnatomyRegion,
    val totalRegionAreaPixels: Int,
    val lesionAreaPixels: Int
) {
    val calculatedBsa: Double
        get() = if (totalRegionAreaPixels > 0) {
            region.bsaPercentageAdult * (lesionAreaPixels.toDouble() / totalRegionAreaPixels.toDouble())
        } else {
            0.0
        }
}