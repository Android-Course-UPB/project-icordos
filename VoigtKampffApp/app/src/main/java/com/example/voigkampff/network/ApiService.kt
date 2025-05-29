package com.example.voigkampff.network

import com.example.voigkampff.data.AnalysisResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/analyze") // Endpoint path
    suspend fun analyzeUsername(@Query("username") username: String): AnalysisResponse
}