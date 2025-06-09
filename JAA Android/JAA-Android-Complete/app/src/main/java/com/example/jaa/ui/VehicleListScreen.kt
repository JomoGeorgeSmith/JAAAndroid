package com.example.jaa.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jaa.model.Vehicle
import androidx.compose.foundation.clickable

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun VehicleListScreen(vehicles: List<Vehicle>, onVehicleClick: (Int) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Vehicles") }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {

            items(vehicles) { vehicle ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onVehicleClick(vehicle.id) }, // clickable here
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${vehicle.make} ${vehicle.model} (${vehicle.year})", style = MaterialTheme.typography.titleMedium)
                        Text("License Plate: ${vehicle.licensePlate}")
                        Text("Fitness Expiration: ${vehicle.formatDate(vehicle.fitnessExpirationDate)}")
                        Text("Insurance Expiration: ${vehicle.formatDate(vehicle.insuranceExpirationDate)}")
                    }
                }
            }
        }
    }
}
