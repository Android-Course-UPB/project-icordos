package com.example.voigkampff.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnalysisResponse(
    @SerialName("username") val username: String,
    @SerialName("bot_probability") val botProbability: Double,
    @SerialName("confidence_score") val confidenceScore: Double,
    @SerialName("classification") val classification: String,
    @SerialName("key_indicators") val keyIndicators: KeyIndicators? // Make nullable if it can be absent
)

@Serializable
data class KeyIndicators(
    @SerialName("activity_pattern") val activityPattern: Double? // Make nullable if it can be absent
    // Add other key indicators here if needed
)