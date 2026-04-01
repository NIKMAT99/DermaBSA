package com.example.dermabsa.model

enum class BodyRegion(val displayName: String, val bsaPercentage: Double) {
    HEAD_FRONT("Testa davanti", 4.5), // [cite: 60]
    HEAD_BACK("Testa dietro", 4.5),
    ARM_LEFT_FRONT("Braccio Sinistro davanti", 4.5), // [cite: 61]
    ARM_RIGHT_FRONT("Braccio Destro davanti", 4.5), // [cite: 61]
    ARM_LEFT_BACK("Braccio Sinistro dietro", 4.5), // [cite: 61]
    ARM_RIGHT_BACK("Braccio Destro dietro", 4.5), // [cite: 61]
    LEG_LEFT_FRONT("Gamba Sinistra davanti", 9.0), // [cite: 62]
    LEG_RIGHT_FRONT("Gamba Destra davanti", 9.0), // [cite: 62]
    LEG_LEFT_BACK("Gamba Sinistra dietro", 9.0), // [cite: 62]
    LEG_RIGHT_BACK("Gamba Destra dietro", 9.0), // [cite: 62]
    TRUNK_FRONT("Tronco davanti", 9.0), // [cite: 63]
    ABDOMEN("Addome",9.0),
    UPPER_BACK("Tronco Posteriore", 9.0),
    LOWER_BACK("Lombare",9.0),
    GENITALS("Genitali", 1.0)
}