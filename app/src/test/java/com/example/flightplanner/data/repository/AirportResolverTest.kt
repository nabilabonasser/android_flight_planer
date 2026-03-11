package com.example.flightplanner.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AirportResolverTest {

    @Test
    fun resolveDestinations_returnsDamascusAndNearbyAirports() {
        val destinations = AirportResolver.resolveDestinations()

        val iataCodes = destinations.map { it.iata }
        assertEquals(listOf("DAM", "BEY", "AMM", "ALP"), iataCodes)
    }

    @Test
    fun resolveOrigin_withCity_returnsKnownNearbyAirports() {
        val dubaiOrigins = AirportResolver.resolveOrigin("dubai")

        assertEquals(listOf("DXB", "DWC"), dubaiOrigins.map { it.iata })
    }

    @Test
    fun resolveOrigin_withIata_returnsSameIataInUppercase() {
        val origin = AirportResolver.resolveOrigin("ist")

        assertEquals(1, origin.size)
        assertEquals("IST", origin.first().iata)
    }

    @Test
    fun resolveOrigin_withUnknownCity_returnsEmptyList() {
        val origin = AirportResolver.resolveOrigin("unknown-city")

        assertTrue(origin.isEmpty())
    }
}
