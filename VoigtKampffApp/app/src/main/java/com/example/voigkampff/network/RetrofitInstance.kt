package com.example.voigkampff.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

object RetrofitInstance {
    // Use 10.0.2.2 for localhost when running on an Android emulator
    // Use your machine's local IP if running on a physical device connected to the same network
    private const val BASE_URL = "http://10.0.2.2:5050/" // Emulator localhost
    // private const val BASE_URL = "http://YOUR_MACHINE_IP:5050/" // Physical device

    private val json = Json {
        ignoreUnknownKeys = true // Important if the API might add new fields
        coerceInputValues = true // Useful for handling potential nulls for non-nullable types if API is lenient
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }
}