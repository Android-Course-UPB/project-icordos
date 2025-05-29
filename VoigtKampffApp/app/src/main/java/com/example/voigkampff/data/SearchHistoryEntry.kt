package com.example.voigkampff.data // Or your preferred data package

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // Auto-generated ID for each entry
    val username: String,
    val timestamp: Long = System.currentTimeMillis(), // Timestamp of the search
    val botProbabilityPercent: Int, // The result
    val isLikelyHuman: Boolean // Store if it was determined as human or bot
)