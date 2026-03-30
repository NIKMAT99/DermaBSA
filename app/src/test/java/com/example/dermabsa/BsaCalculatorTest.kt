package com.example.dermabsa

import com.example.dermabsa.model.BodyRegion
import com.example.dermabsa.utils.BsaCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BsaCalculatorTest {

    @Test
    fun testCalculateLesionBsa_SlideExample() {
        // Riproduciamo fedelmente l'esempio delle slide:
        // - regione: schiena (13%)
        // - area lesione stimata: 25% del distretto
        // - Output atteso: 3.25%

        // Simuliamo l'algoritmo visivo: diciamo che il distretto è grande 1000 pixel
        // e la lesione ne occupa 250 (il 25% esatto).
        val result = BsaCalculator.calculateLesionBsa(
            region = BodyRegion.TRUNK_BACK,
            lesionAreaPixels = 250,
            regionTotalAreaPixels = 1000
        )

        // Verifichiamo che il risultato sia 3.25 (il parametro 0.001 è la tolleranza per i decimali)
        assertEquals(3.25, result.finalInvolvedPercentage, 0.001)
    }

    @Test
    fun testCalculateLesionBsa_InvalidInput_ThrowsException() {
        // Testiamo la robustezza: cosa succede se la OpenCV o l'utente passano dati sballati?
        // Ad esempio, un'area del distretto pari a 0 (che causerebbe una divisione per zero).

        assertThrows(IllegalArgumentException::class.java) {
            BsaCalculator.calculateLesionBsa(
                region = BodyRegion.ARM_LEFT,
                lesionAreaPixels = 500,
                regionTotalAreaPixels = 0 // Valore non valido!
            )
        }
    }
}