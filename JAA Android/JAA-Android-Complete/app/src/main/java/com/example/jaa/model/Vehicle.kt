package com.example.jaa.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class Vehicle(
    @SerializedName("vehicle_id") val id: Int,
    @SerializedName("customer_id") val customerId: Int,
    val make: String,
    val model: String,
    val year: Int,
    @SerializedName("license_plate") val licensePlate: String,
    @SerializedName("fitness_expiration_date") val fitnessExpirationDate: String,
    @SerializedName("insurance_expiration_date") val insuranceExpirationDate: String
) {
    fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)
            val date = inputFormat.parse(dateString)
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
}