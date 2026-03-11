package com.example.flightplanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flightplanner.data.repository.FlightSearchRepository
import com.example.flightplanner.ui.FlightSearchScreen
import com.example.flightplanner.ui.FlightSearchViewModel
import com.example.flightplanner.ui.FlightSearchViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = FlightSearchRepository.default()

        setContent {
            val vm: FlightSearchViewModel = viewModel(
                factory = FlightSearchViewModelFactory(repository)
            )

            MaterialTheme {
                Surface {
                    FlightSearchScreen(
                        state = vm.state,
                        onSearch = vm::search,
                        onOpenDeal = { url ->
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    )
                }
            }
        }
    }
}
