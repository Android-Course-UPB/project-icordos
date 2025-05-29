package com.example.voigkampff.ui // Or your chosen package, e.g., com.example.voigkampff.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.voigkampff.data.AnalysisResponse
import com.example.voigkampff.network.ApiService // Assuming ApiService is in the network package
import com.example.voigkampff.network.RetrofitInstance
import com.example.voigkampff.data.SearchHistoryDao
import com.example.voigkampff.data.SearchHistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import retrofit2.HttpException // For HTTP errors (non-2xx responses)
import android.util.Log

// Define the different states for the UI
sealed interface AnalysisUiState {
    data object Idle : AnalysisUiState // Initial state, nothing happening
    data object Loading : AnalysisUiState // API call in progress
    data class Success(val analysis: AnalysisResponse) : AnalysisUiState // API call successful
    data class Error(val message: String) : AnalysisUiState // API call failed
}

class AnalysisViewModel(
    private val apiService: ApiService = RetrofitInstance.api, // Default instance, can be injected for testing
    private val searchHistoryDao: SearchHistoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    fun analyzeUsername(username: String) {
        // Don't start a new request if one is already loading
        if (_uiState.value == AnalysisUiState.Loading) {
            return
        }

        viewModelScope.launch {
            _uiState.value = AnalysisUiState.Loading
            try {
                val response = apiService.analyzeUsername(username)
                _uiState.value = AnalysisUiState.Success(response)

                // Save to search history after successful API call
                saveToSearchHistory(response)
            } catch (e: IOException) {
                // Network errors (e.g., no internet connection, server unreachable)
                _uiState.value = AnalysisUiState.Error("Network error: ${e.localizedMessage ?: "Check connection"}")
            } catch (e: HttpException) {
                // HTTP errors (e.g., 404 Not Found, 500 Internal Server Error)
                val errorBody = e.response()?.errorBody()?.string()
                _uiState.value = AnalysisUiState.Error(
                    "API error: ${e.code()} - ${e.message()} " +
                            (if (!errorBody.isNullOrBlank()) "\nDetails: $errorBody" else "")
                )
            } catch (e: Exception) {
                // Other unexpected errors
                _uiState.value = AnalysisUiState.Error("An unexpected error occurred: ${e.localizedMessage}")
            }
        }
    }

    private fun saveToSearchHistory(analysisResponse: AnalysisResponse) {
        viewModelScope.launch { // Use viewModelScope for database operations
            try {
                // Adjust field names if they are different in your AnalysisResponse
                // e.g., analysisResponse.bot_probability if your JSON field is "bot_probability"
                val username =
                    analysisResponse.username ?: "Unknown User" // Handle possible null username
                val botProbabilityPercent = (analysisResponse.botProbability * 100).toInt()
                val isLikelyHuman = botProbabilityPercent < 50

                val searchEntry = SearchHistoryEntry(
                    username = username,
                    botProbabilityPercent = botProbabilityPercent,
                    isLikelyHuman = isLikelyHuman,
                    timestamp = System.currentTimeMillis()
                )
                searchHistoryDao.insertSearchEntry(searchEntry)
                Log.d("AnalysisViewModel", "Search entry saved for $username")
            } catch (e: Exception) {
                Log.e("AnalysisViewModel", "Error saving search entry", e)
                // Optionally, you could emit another UI state here if saving fails
                // but it might complicate things if the main API call was successful.
                // For now, just logging the error.
            }
        }
    }



    /**
     * Resets the UI state back to Idle.
     * Useful when navigating back to the landing screen or after displaying an error.
     */
    fun resetState() {
        _uiState.value = AnalysisUiState.Idle
    }
}

