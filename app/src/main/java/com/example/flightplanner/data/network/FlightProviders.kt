package com.example.flightplanner.data.network

import com.example.flightplanner.BuildConfig
import com.example.flightplanner.data.model.Airport
import com.example.flightplanner.data.model.CabinPreference
import com.example.flightplanner.data.model.FlightDeal
import com.example.flightplanner.data.model.FlightSearchRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface FlightProvider {
    suspend fun search(request: FlightSearchRequest, origins: List<Airport>, destinations: List<Airport>): List<FlightDeal>
}

private class SkyScannerProvider(private val api: SkyScannerApi) : FlightProvider {
    override suspend fun search(
        request: FlightSearchRequest,
        origins: List<Airport>,
        destinations: List<Airport>
    ): List<FlightDeal> {
        if (BuildConfig.SKYSCANNER_API_KEY.isBlank()) return emptyList()
        val origin = origins.firstOrNull()?.iata ?: return emptyList()
        val destination = destinations.firstOrNull()?.iata ?: return emptyList()

        val response = api.search(
            apiKey = BuildConfig.SKYSCANNER_API_KEY,
            origin = origin,
            destination = destination,
            date = request.departureDate.toString(),
            adults = request.passengers.adults,
            children = request.passengers.children2To12,
            infants = request.passengers.infants0To2
        )

        return response.results.map {
            FlightDeal(
                provider = "Skyscanner",
                title = it.legs.firstOrNull()?.airline ?: "Skyscanner option",
                fromAirport = it.origin,
                toAirport = it.destination,
                duration = it.duration,
                stops = it.stops,
                priceLabel = it.price,
                deepLink = it.deepLink
            )
        }
    }
}

private class GoogleFlightsProvider(private val api: SerpApi) : FlightProvider {
    override suspend fun search(
        request: FlightSearchRequest,
        origins: List<Airport>,
        destinations: List<Airport>
    ): List<FlightDeal> {
        if (BuildConfig.SERP_API_KEY.isBlank()) return emptyList()
        val origin = origins.firstOrNull()?.iata ?: return emptyList()
        val destination = destinations.firstOrNull()?.iata ?: return emptyList()

        val data = api.searchGoogleFlights(
            apiKey = BuildConfig.SERP_API_KEY,
            departureId = origin,
            arrivalId = destination,
            outboundDate = request.departureDate.toString(),
            returnDate = request.returnDate?.toString(),
            adults = request.passengers.adults,
            children = request.passengers.children2To12,
            infantsInSeat = request.passengers.infants0To2
        )

        return data.bestFlights.orEmpty().map {
            FlightDeal(
                provider = "Google Flights",
                title = it.flights.joinToString(" + ") { segment -> segment.airline },
                fromAirport = origin,
                toAirport = destination,
                duration = it.totalDuration,
                stops = it.flights.size - 1,
                priceLabel = it.price,
                deepLink = data.searchMetadata.googleFlightsUrl
            )
        }
    }
}

private class DemoPortalProvider : FlightProvider {
    override suspend fun search(
        request: FlightSearchRequest,
        origins: List<Airport>,
        destinations: List<Airport>
    ): List<FlightDeal> {
        return origins.flatMap { origin ->
            destinations.map { destination ->
                FlightDeal(
                    provider = "Known Portals",
                    title = "${origin.city} → ${destination.city}",
                    fromAirport = origin.iata,
                    toAirport = destination.iata,
                    duration = "6h 25m",
                    stops = if (request.cabinPreference == CabinPreference.DIRECT_ONLY) 0 else 1,
                    priceLabel = "from $320",
                    deepLink = "https://www.kiwi.com/en/search/results/${origin.iata}/${destination.iata}/${request.departureDate}"
                )
            }
        }
    }
}

private interface SkyScannerApi {
    @GET("api/v1/flights/search")
    suspend fun search(
        @Query("apiKey") apiKey: String,
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("date") date: String,
        @Query("adults") adults: Int,
        @Query("children") children: Int,
        @Query("infants") infants: Int
    ): SkyScannerResponse
}

private interface SerpApi {
    @GET("search.json?engine=google_flights")
    suspend fun searchGoogleFlights(
        @Query("api_key") apiKey: String,
        @Query("departure_id") departureId: String,
        @Query("arrival_id") arrivalId: String,
        @Query("outbound_date") outboundDate: String,
        @Query("return_date") returnDate: String?,
        @Query("adults") adults: Int,
        @Query("children") children: Int,
        @Query("infants_in_seat") infantsInSeat: Int
    ): SerpFlightsResponse
}

@Serializable
private data class SkyScannerResponse(val results: List<SkyScannerResult> = emptyList())

@Serializable
private data class SkyScannerResult(
    val origin: String,
    val destination: String,
    val duration: String,
    val stops: Int,
    val price: String,
    @SerialName("deeplink") val deepLink: String,
    val legs: List<SkyScannerLeg> = emptyList()
)

@Serializable
private data class SkyScannerLeg(val airline: String)

@Serializable
private data class SerpFlightsResponse(
    @SerialName("best_flights") val bestFlights: List<SerpBestFlight>? = null,
    @SerialName("search_metadata") val searchMetadata: SerpSearchMetadata
)

@Serializable
private data class SerpSearchMetadata(
    @SerialName("google_flights_url") val googleFlightsUrl: String
)

@Serializable
private data class SerpBestFlight(
    @SerialName("total_duration") val totalDuration: String,
    val price: String,
    val flights: List<SerpFlight>
)

@Serializable
private data class SerpFlight(val airline: String)

object FlightProviderFactory {
    private val json = Json { ignoreUnknownKeys = true }

    private val retrofitBuilder = Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()
        )

    fun skyscannerProvider(): FlightProvider = SkyScannerProvider(
        retrofitBuilder
            .baseUrl("https://partners.api.skyscanner.net/")
            .build()
            .create(SkyScannerApi::class.java)
    )

    fun googleFlightsProvider(): FlightProvider = GoogleFlightsProvider(
        retrofitBuilder
            .baseUrl("https://serpapi.com/")
            .build()
            .create(SerpApi::class.java)
    )

    fun portalProvider(): FlightProvider = DemoPortalProvider()
}
