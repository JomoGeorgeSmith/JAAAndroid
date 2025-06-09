package com.example.jaa.model

data class SchedulePickupRequest(
    val vehicle_id: String,
    val customer_id: String,
    val pickup_date: String,
    val location: String
)