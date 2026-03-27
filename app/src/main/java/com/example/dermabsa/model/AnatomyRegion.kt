package com.example.dermabsa.model

enum class AnatomyRegion(val displayName: String, val bsaPercentageAdult: Double) {
    HEAD("Testa e Collo", 9.0),
    FRONT_TRUNK("Tronco Anteriore", 18.0),
    BACK_TRUNK("Tronco Posteriore", 18.0),
    RIGHT_ARM("Braccio Destro", 9.0),
    LEFT_ARM("Braccio Sinistro", 9.0),
    RIGHT_LEG("Gamba Destra", 18.0),
    LEFT_LEG("Gamba Sinistra", 18.0),
    GENITALS("Genitali", 1.0)
}