package com.example.jaa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.*
import com.example.jaa.model.Vehicle
import com.example.jaa.ui.*
import com.example.jaa.ui.theme.JAATheme
import android.content.Context

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestNotificationPermission()

        val prefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val savedEmail = prefs.getString("user_email", null)

        setContent {
            JAATheme {
                val navController = rememberNavController()
                val vehiclesState = remember { mutableStateOf<List<Vehicle>>(emptyList()) }
                val emailState = remember { mutableStateOf("") }
                val customerIdState = remember { mutableStateOf<Int?>(null) }

                var initialRoute by remember { mutableStateOf(if (savedEmail != null) "vehicles" else "login") }

                // Handle initial intent (cold start)
                val targetScreen = intent?.getStringExtra("navigate_to")
                val vehicleIdFromIntent = intent?.getIntExtra("vehicleId", -1)
                val customerIdFromIntent = intent?.getIntExtra("customerId", -1)
                if (targetScreen == "schedule" && vehicleIdFromIntent != null && vehicleIdFromIntent > 0) {
                    initialRoute = "schedule/$vehicleIdFromIntent"
                    if (customerIdFromIntent != null && customerIdFromIntent > 0) {
                        customerIdState.value = customerIdFromIntent
                    }
                }

                // Listen for new intents (when app is already running)
                val currentIntent by rememberUpdatedState(intent)
                LaunchedEffect(currentIntent) {
                    val navTarget = currentIntent?.getStringExtra("navigate_to")
                    val vehicleId = currentIntent?.getIntExtra("vehicleId", -1)
                    val customerId = currentIntent?.getIntExtra("customerId", -1)
                    if (navTarget == "schedule" && vehicleId != null && vehicleId > 0) {
                        if (customerId != null && customerId > 0) {
                            customerIdState.value = customerId
                        }
                        navController.navigate("schedule/$vehicleId") {
                            popUpTo("vehicles") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }

                NavHost(navController = navController, startDestination = initialRoute) {
                    composable("login") {
                        LoginScreen(navController) { vehicles, email, customerId ->
                            vehiclesState.value = vehicles
                            emailState.value = email
                            customerIdState.value = customerId
                            navController.navigate("vehicles")
                        }
                    }
                    composable("vehicles") {
                        VehicleListScreen(vehicles = vehiclesState.value) { vehicleId ->
                            navController.navigate("schedule/$vehicleId")
                        }
                    }
                    composable("schedule/{vehicleId}") { backStackEntry ->
                        val vehicleId = backStackEntry.arguments?.getString("vehicleId")?.toIntOrNull()
                        val customerId = customerIdState.value
                        if (vehicleId != null && customerId != null) {
                            SchedulePickupScreen(vehicleId = vehicleId, customerId = customerId)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent) // Updates the intent for Compose to observe
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}