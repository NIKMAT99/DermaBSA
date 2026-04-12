package com.example.dermabsa.utils

import com.example.dermabsa.model.BodyRegion
import com.example.dermabsa.model.BsaResult

object BsaCalculator {
    fun getRegionPercentage(region: BodyRegion): Double {
        return when (region) {
            BodyRegion.HEAD_FRONT, BodyRegion.HEAD_BACK -> 3.5
            BodyRegion.NECK_FRONT, BodyRegion.NECK_BACK -> 1.0
            BodyRegion.CHEST, BodyRegion.ABDOMEN -> 6.5
            BodyRegion.UPPER_BACK, BodyRegion.LOWER_BACK -> 6.5
            BodyRegion.UPPER_ARM_LEFT_FRONT, BodyRegion.UPPER_ARM_RIGHT_FRONT,
            BodyRegion.UPPER_ARM_LEFT_BACK, BodyRegion.UPPER_ARM_RIGHT_BACK -> 2.0
            BodyRegion.FOREARM_LEFT_FRONT, BodyRegion.FOREARM_RIGHT_FRONT,
            BodyRegion.FOREARM_LEFT_BACK, BodyRegion.FOREARM_RIGHT_BACK -> 1.5
            BodyRegion.HAND_LEFT_FRONT, BodyRegion.HAND_RIGHT_FRONT,
            BodyRegion.HAND_LEFT_BACK, BodyRegion.HAND_RIGHT_BACK -> 1.25
            BodyRegion.GENITALS -> 1.0
            BodyRegion.BUTTOCK_LEFT, BodyRegion.BUTTOCK_RIGHT -> 2.5
            BodyRegion.THIGH_LEFT_FRONT, BodyRegion.THIGH_RIGHT_FRONT,
            BodyRegion.THIGH_LEFT_BACK, BodyRegion.THIGH_RIGHT_BACK -> 4.5
            BodyRegion.LOWER_LEG_LEFT_FRONT, BodyRegion.LOWER_LEG_RIGHT_FRONT,
            BodyRegion.LOWER_LEG_LEFT_BACK, BodyRegion.LOWER_LEG_RIGHT_BACK -> 4.0
            BodyRegion.FOOT_LEFT_FRONT, BodyRegion.FOOT_RIGHT_FRONT,
            BodyRegion.FOOT_LEFT_BACK, BodyRegion.FOOT_RIGHT_BACK -> 1.5
        }
    }

    fun calculateLesionBsa(region: BodyRegion, lesionAreaPixels: Int, regionTotalAreaPixels: Int): BsaResult {
        val regionMaxBsa = getRegionPercentage(region)
        val coverageFraction = if (regionTotalAreaPixels > 0) {
            lesionAreaPixels.toDouble() / regionTotalAreaPixels.toDouble()
        } else {
            0.0
        }

        val calculatedBsa = coverageFraction * regionMaxBsa

        return BsaResult(
            region = region,
            lesionAreaPixels = lesionAreaPixels,
            regionTotalAreaPixels = regionTotalAreaPixels,
            finalInvolvedPercentage = calculatedBsa.coerceAtMost(regionMaxBsa)
        )
    }
}