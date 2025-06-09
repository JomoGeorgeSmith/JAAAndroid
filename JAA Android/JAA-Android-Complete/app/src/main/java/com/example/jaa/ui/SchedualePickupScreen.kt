package com.example.jaa.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.jaa.model.SchedulePickupRequest
import com.example.jaa.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun SchedulePickupScreen(vehicleId: Int, customerId: Int) {
    val context = LocalContext.current
    var pickupDate by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Schedule Pickup", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = pickupDate,
            onValueChange = { pickupDate = it },
            label = { Text("Pickup Date (YYYY-MM-DD)") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") }
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            val requestData = SchedulePickupRequest(
                vehicle_id = vehicleId.toString(),
                customer_id = customerId.toString(),
                pickup_date = "${pickupDate}T00:00:00Z",
                location = location
            )

            RetrofitClient.api.schedulePickup(requestData).enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(
                    call: Call<Map<String, String>>,
                    response: Response<Map<String, String>>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Pickup scheduled!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to schedule pickup", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
        }) {
            Text("Submit")
        }
    }
}
