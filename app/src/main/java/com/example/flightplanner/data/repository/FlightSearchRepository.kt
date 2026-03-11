package com.example.flightplanner.data.repository

import com.example.flightplanner.data.model.FlightDeal
import com.example.flightplanner.data.model.FlightSearchRequest
import com.example.flightplanner.data.network.FlightProvider
import com.example.flightplanner.data.network.FlightProviderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class FlightSearchRepository(
    private val providers: List<FlightProvider>
) {
    suspend fun search(request: FlightSearchRequest): List<FlightDeal> = withContext(Dispatchers.IO) {
        val origins = AirportResolver.resolveOrigin(request.originQuery)
        val destinations = AirportResolver.resolveDestinations()

        if (origins.isEmpty()) return@withContext emptyList()

        coroutineScope {
            providers.map { provider ->
                async {
                    runCatching { provider.search(request, origins, destinations) }
                        .getOrElse { emptyList() }
                }
            }.awaitAll().flatten().sortedBy { it.priceLabel }
        }
    }

    companion object {
        fun default() = FlightSearchRepository(
            providers = listOf(
                FlightProviderFactory.skyscannerProvider(),
                FlightProviderFactory.googleFlightsProvider(),
                FlightProviderFactory.portalProvider()
            )
        )
    }
}
