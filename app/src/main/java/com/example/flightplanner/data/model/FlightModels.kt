package com.example.flightplanner.data.model

import java.time.LocalDate

enum class CabinPreference { ANY, DIRECT_ONLY, TRANSIT_ALLOWED }
enum class TripType { ONE_WAY, ROUND_TRIP }

data class PassengerInfo(
    val adults: Int,
    val infants0To2: Int,
    val children2To12: Int
)

data class FlightSearchRequest(
    val originQuery: String,
    val destinationHint: String = "Damascus",
    val tripType: TripType,
    val departureDate: LocalDate,
    val returnDate: LocalDate?,
    val passengers: PassengerInfo,
    val cabinPreference: CabinPreference
)

data class Airport(
    val iata: String,
    val city: String,
    val country: String
)

data class FlightDeal(
    val provider: String,
    val title: String,
    val fromAirport: String,
    val toAirport: String,
    val duration: String,
    val stops: Int,
    val priceLabel: String,
    val deepLink: String
)
