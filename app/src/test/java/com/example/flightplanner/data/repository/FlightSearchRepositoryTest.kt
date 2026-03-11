package com.example.flightplanner.data.repository

import com.example.flightplanner.data.model.Airport
import com.example.flightplanner.data.model.CabinPreference
import com.example.flightplanner.data.model.FlightDeal
import com.example.flightplanner.data.model.FlightSearchRequest
import com.example.flightplanner.data.model.PassengerInfo
import com.example.flightplanner.data.model.TripType
import com.example.flightplanner.data.network.FlightProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FlightSearchRepositoryTest {

    @Test
    fun search_withUnknownOrigin_returnsEmptyList() = runBlocking {
        val repository = FlightSearchRepository(
            providers = listOf(FakeProvider(emptyList()))
        )

        val results = repository.search(request(origin = "unknown-city"))

        assertTrue(results.isEmpty())
    }

    @Test
    fun search_aggregatesAndSortsResultsFromProviders() = runBlocking {
        val expensive = FlightDeal(
            provider = "P1",
            title = "A",
            fromAirport = "DXB",
            toAirport = "DAM",
            duration = "2h",
            stops = 0,
            priceLabel = "from $900",
            deepLink = "https://example.com/1"
        )
        val cheap = FlightDeal(
            provider = "P2",
            title = "B",
            fromAirport = "DXB",
            toAirport = "BEY",
            duration = "2h",
            stops = 1,
            priceLabel = "from $100",
            deepLink = "https://example.com/2"
        )

        val repository = FlightSearchRepository(
            providers = listOf(
                FakeProvider(listOf(expensive)),
                FakeProvider(listOf(cheap))
            )
        )

        val results = repository.search(request(origin = "dubai"))

        assertEquals(2, results.size)
        assertEquals(listOf("from $100", "from $900"), results.map { it.priceLabel })
    }

    private fun request(origin: String) = FlightSearchRequest(
        originQuery = origin,
        tripType = TripType.ONE_WAY,
        departureDate = LocalDate.now().plusDays(14),
        returnDate = null,
        passengers = PassengerInfo(adults = 1, infants0To2 = 0, children2To12 = 0),
        cabinPreference = CabinPreference.TRANSIT_ALLOWED
    )

    private class FakeProvider(private val data: List<FlightDeal>) : FlightProvider {
        override suspend fun search(
            request: FlightSearchRequest,
            origins: List<Airport>,
            destinations: List<Airport>
        ): List<FlightDeal> = data
    }
}
