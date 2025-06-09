package com.example.jaa.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.jaa.model.Vehicle
import com.example.jaa.network.RetrofitClient
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: (List<Vehicle>, String, Int) -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("JAA Login", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                loading = true
                val credentials = mapOf("email" to email, "password" to password)

                RetrofitClient.api.authenticateUser(credentials)
                    .enqueue(object : Callback<com.example.jaa.model.AuthResponse> {
                        override fun onResponse(
                            call: Call<com.example.jaa.model.AuthResponse>,
                            response: Response<com.example.jaa.model.AuthResponse>
                        ) {
                            loading = false
                            if (response.isSuccessful && response.body()?.success == true) {
                                val vehicles = response.body()?.vehicles ?: emptyList()
                                val customerId = response.body()?.vehicles?.firstOrNull()?.customerId

                                if (customerId != null) {
                                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val token = task.result
                                            val update = mapOf("email" to email, "fcmToken" to token)
                                            RetrofitClient.api.updateToken(update)
                                                .enqueue(object : Callback<Void> {
                                                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                                        Log.d("LoginScreen", "FCM token updated.")
                                                    }

                                                    override fun onFailure(call: Call<Void>, t: Throwable) {
                                                        Log.e("LoginScreen", "FCM token update failed: ${t.localizedMessage}")
                                                    }
                                                })
                                        }
                                    }

                                    onLoginSuccess(vehicles, email, customerId)
                                } else {
                                    Toast.makeText(context, "Unable to retrieve customer ID", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, "Invalid email or password", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(
                            call: Call<com.example.jaa.model.AuthResponse>,
                            t: Throwable
                        ) {
                            loading = false
                            Toast.makeText(context, "Error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    })
            },
            enabled = !loading
        ) {
            Text(if (loading) "Signing In..." else "Sign In")
        }
    }
}
