package com.example.dermabsa.utils

import com.example.dermabsa.model.BodyRegion
import com.example.dermabsa.model.BsaResult

/**
 * Motore di calcolo per stimare la percentuale di superficie corporea (BSA) coinvolta.
 */
object BsaCalculator {

    /**
     * Calcola la BSA della lesione basandosi sull'area in pixel.
     * * @param region Il distretto anatomico selezionato (es. Schiena al 13%).
     * @param lesionAreaPixels I pixel totali evidenziati come lesione.
     * @param regionTotalAreaPixels I pixel totali che compongono il distretto anatomico nell'immagine.
     * @return Un oggetto BsaResult con i dati completi.
     */
    fun calculateLesionBsa(
        region: BodyRegion,
        lesionAreaPixels: Int,
        regionTotalAreaPixels: Int
    ): BsaResult {

        // Gestione degli errori: preveniamo divisioni per zero o dati sballati
        if (regionTotalAreaPixels <= 0) {
            throw IllegalArgumentException("L'area totale del distretto deve essere maggiore di zero.")
        }
        if (lesionAreaPixels < 0 || lesionAreaPixels > regionTotalAreaPixels) {
            throw IllegalArgumentException("L'area della lesione non è valida rispetto all'area totale.")
        }

        // 1. Calcolo del rapporto tra l'area della lesione e l'area del distretto
        val areaRatio = lesionAreaPixels.toDouble() / regionTotalAreaPixels.toDouble()

        // 2. Calcolo della percentuale finale applicando la Rule of Nines
        val finalPercentage = region.bsaPercentage * areaRatio

        // 3. Restituiamo il risultato pacchettizzato per la UI
        return BsaResult(
            region = region,
            lesionAreaPixels = lesionAreaPixels,
            regionTotalAreaPixels = regionTotalAreaPixels,
            finalInvolvedPercentage = finalPercentage
        )
    }
}