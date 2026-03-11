package com.example.flightplanner.data.repository

import com.example.flightplanner.data.model.Airport

object AirportResolver {
    private val destinationAirports = listOf(
        Airport("DAM", "Damascus", "Syria"),
        Airport("BEY", "Beirut", "Lebanon"),
        Airport("AMM", "Amman", "Jordan"),
        Airport("ALP", "Aleppo", "Syria")
    )

    private val fallbackOrigins = mapOf(
        "damascus" to listOf(Airport("DAM", "Damascus", "Syria")),
        "beirut" to listOf(Airport("BEY", "Beirut", "Lebanon")),
        "amman" to listOf(Airport("AMM", "Amman", "Jordan")),
        "aleppo" to listOf(Airport("ALP", "Aleppo", "Syria")),
        "dubai" to listOf(Airport("DXB", "Dubai", "UAE"), Airport("DWC", "Dubai", "UAE")),
        "istanbul" to listOf(Airport("IST", "Istanbul", "Türkiye"), Airport("SAW", "Istanbul", "Türkiye")),
        "riyadh" to listOf(Airport("RUH", "Riyadh", "Saudi Arabia"))
    )

    fun resolveOrigin(query: String): List<Airport> {
        val normalized = query.trim().lowercase()
        if (normalized.length == 3) {
            return listOf(Airport(normalized.uppercase(), normalized.uppercase(), "Unknown"))
        }
        return fallbackOrigins[normalized] ?: emptyList()
    }

    fun resolveDestinations(): List<Airport> = destinationAirports
}
