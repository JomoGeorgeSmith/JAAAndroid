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
import android.app.DatePickerDialog
import androidx.compose.runtime.remember
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulePickupScreen(vehicleId: Int, customerId: Int) {
    val context = LocalContext.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    var pickupDate by remember { mutableStateOf("") }
    val calendar = remember { Calendar.getInstance() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Pickup") },
                navigationIcon = {
                    IconButton(onClick = { backDispatcher?.onBackPressed() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Button(onClick = {
                val datePicker = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        pickupDate = format.format(calendar.time)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePicker.show()
            }) {
                Text(if (pickupDate.isEmpty()) "Select Pickup Date" else "Pickup Date: $pickupDate")
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                val requestData = SchedulePickupRequest(
                    vehicle_id = vehicleId.toString(),
                    customer_id = customerId.toString(),
                    pickup_date = if (pickupDate.isNotEmpty()) "${pickupDate}T00:00:00Z" else "",
                    location = "Home"
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
            }, enabled = pickupDate.isNotEmpty()) {
                Text("Submit")
            }
        }
    }
}