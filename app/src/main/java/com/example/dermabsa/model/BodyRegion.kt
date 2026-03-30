package com.example.dermabsa.model

enum class BodyRegion(val displayName: String, val bsaPercentage: Double) {
    HEAD("Testa", 9.0), // [cite: 60]
    ARM_LEFT("Braccio Sinistro", 9.0), // [cite: 61]
    ARM_RIGHT("Braccio Destro", 9.0), // [cite: 61]
    LEG_LEFT("Gamba Sinistra", 18.0), // [cite: 62]
    LEG_RIGHT("Gamba Destra", 18.0), // [cite: 62]
    TRUNK_FRONT("Tronco Anteriore", 18.0), // [cite: 63]
    TRUNK_BACK("Tronco Posteriore", 13.0) // Esempio specifico fornito per il tronco posteriore
}