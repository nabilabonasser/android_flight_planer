package com.example.flightplanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.flightplanner.R
import com.example.flightplanner.data.model.CabinPreference
import com.example.flightplanner.data.model.FlightDeal
import com.example.flightplanner.data.model.TripType

@Composable
fun FlightSearchScreen(
    state: FlightSearchUiState,
    onSearch: (FlightSearchUiState) -> Unit,
    onOpenDeal: (String) -> Unit
) {
    var draft by remember(state.origin, state.tripType, state.departureDate, state.returnDate, state.adults, state.infants0To2, state.children2To12, state.preference) {
        mutableStateOf(state.copy(error = null, results = emptyList(), isLoading = false))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(stringResource(R.string.search_title))
        }
        item {
            OutlinedTextField(
                value = draft.origin,
                onValueChange = { draft = draft.copy(origin = it) },
                label = { Text(stringResource(R.string.origin_label)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            TripTypePicker(draft.tripType) { draft = draft.copy(tripType = it) }
        }
        item {
            OutlinedTextField(
                value = draft.departureDate,
                onValueChange = { draft = draft.copy(departureDate = it) },
                label = { Text(stringResource(R.string.departure_date_label)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (draft.tripType == TripType.ROUND_TRIP) {
            item {
                OutlinedTextField(
                    value = draft.returnDate,
                    onValueChange = { draft = draft.copy(returnDate = it) },
                    label = { Text(stringResource(R.string.return_date_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            PassengerFields(
                adults = draft.adults,
                infants = draft.infants0To2,
                children = draft.children2To12,
                onAdults = { draft = draft.copy(adults = it) },
                onInfants = { draft = draft.copy(infants0To2 = it) },
                onChildren = { draft = draft.copy(children2To12 = it) }
            )
        }
        item {
            PreferencePicker(draft.preference) { draft = draft.copy(preference = it) }
        }
        item {
            Button(
                onClick = { onSearch(draft) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.search_portals)) }
        }

        if (state.isLoading) {
            item { CircularProgressIndicator() }
        }
        state.error?.let { error ->
            item {
                val errorText = when (error) {
                    SearchError.INVALID_VALUES -> stringResource(R.string.invalid_values_error)
                    SearchError.NO_FLIGHTS -> stringResource(R.string.no_flights_error)
                }
                Text(errorText)
            }
        }

        items(state.results) { deal ->
            DealCard(deal, onOpenDeal)
        }
    }
}

@Composable
private fun TripTypePicker(selected: TripType, onChange: (TripType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Row {
            RadioButton(selected = selected == TripType.ONE_WAY, onClick = { onChange(TripType.ONE_WAY) })
            Text(stringResource(R.string.one_way))
        }
        Row {
            RadioButton(selected = selected == TripType.ROUND_TRIP, onClick = { onChange(TripType.ROUND_TRIP) })
            Text(stringResource(R.string.round_trip))
        }
    }
}

@Composable
private fun PassengerFields(
    adults: String,
    infants: String,
    children: String,
    onAdults: (String) -> Unit,
    onInfants: (String) -> Unit,
    onChildren: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = adults, onValueChange = onAdults, label = { Text(stringResource(R.string.adults)) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = infants, onValueChange = onInfants, label = { Text(stringResource(R.string.children_0_2)) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = children, onValueChange = onChildren, label = { Text(stringResource(R.string.children_2_12)) }, modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreferencePicker(selected: CabinPreference, onChange: (CabinPreference) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = CabinPreference.entries

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = preferenceLabel(selected),
            onValueChange = {},
            label = { Text(stringResource(R.string.flight_preference)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { pref ->
                DropdownMenuItem(
                    text = { Text(preferenceLabel(pref)) },
                    onClick = {
                        onChange(pref)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun preferenceLabel(pref: CabinPreference): String {
    return when (pref) {
        CabinPreference.ANY -> stringResource(R.string.preference_any)
        CabinPreference.DIRECT_ONLY -> stringResource(R.string.preference_direct_only)
        CabinPreference.TRANSIT_ALLOWED -> stringResource(R.string.preference_transit_allowed)
    }
}

@Composable
private fun DealCard(deal: FlightDeal, onOpenDeal: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDeal(deal.deepLink) }
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${deal.provider} • ${deal.title}")
            Text("${deal.fromAirport} → ${deal.toAirport}")
            Text(stringResource(R.string.deal_stops_line, deal.duration, deal.stops))
            Text(deal.priceLabel)
            Text(stringResource(R.string.tap_to_continue))
        }
    }
}
