package com.example.jaa.network

import com.example.jaa.model.AuthResponse
import  com.example.jaa.model.SchedulePickupRequest
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("authenticate")
    fun authenticateUser(@Body credentials: Map<String, String>): Call<AuthResponse>

    @POST("updateToken")
    fun updateToken(@Body tokenUpdate: Map<String, String>): Call<Void>

    @POST("schedulePickup")
    fun schedulePickup(@Body request: SchedulePickupRequest): Call<Map<String, String>>



}