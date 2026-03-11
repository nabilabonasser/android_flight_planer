package com.example.flightplanner.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flightplanner.data.model.CabinPreference
import com.example.flightplanner.data.model.FlightDeal
import com.example.flightplanner.data.model.FlightSearchRequest
import com.example.flightplanner.data.model.PassengerInfo
import com.example.flightplanner.data.model.TripType
import com.example.flightplanner.data.repository.FlightSearchRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class SearchError {
    INVALID_VALUES,
    NO_FLIGHTS
}

data class FlightSearchUiState(
    val origin: String = "",
    val tripType: TripType = TripType.ONE_WAY,
    val departureDate: String = LocalDate.now().plusDays(14).toString(),
    val returnDate: String = LocalDate.now().plusDays(21).toString(),
    val adults: String = "1",
    val infants0To2: String = "0",
    val children2To12: String = "0",
    val preference: CabinPreference = CabinPreference.TRANSIT_ALLOWED,
    val isLoading: Boolean = false,
    val error: SearchError? = null,
    val results: List<FlightDeal> = emptyList()
)

class FlightSearchViewModel(private val repository: FlightSearchRepository) : ViewModel() {
    var state by mutableStateOf(FlightSearchUiState())
        private set

    fun search(newState: FlightSearchUiState) {
        state = newState.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val request = runCatching {
                FlightSearchRequest(
                    originQuery = newState.origin,
                    tripType = newState.tripType,
                    departureDate = LocalDate.parse(newState.departureDate),
                    returnDate = newState.returnDate.takeIf { newState.tripType == TripType.ROUND_TRIP }?.let(LocalDate::parse),
                    passengers = PassengerInfo(
                        adults = newState.adults.toInt(),
                        infants0To2 = newState.infants0To2.toInt(),
                        children2To12 = newState.children2To12.toInt()
                    ),
                    cabinPreference = newState.preference
                )
            }.getOrElse {
                state = newState.copy(isLoading = false, error = SearchError.INVALID_VALUES)
                return@launch
            }

            val results = repository.search(request)
            state = newState.copy(
                isLoading = false,
                results = results,
                error = if (results.isEmpty()) SearchError.NO_FLIGHTS else null
            )
        }
    }
}

class FlightSearchViewModelFactory(
    private val repository: FlightSearchRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FlightSearchViewModel(repository) as T
    }
}
