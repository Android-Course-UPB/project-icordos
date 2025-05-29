package com.example.voigkampff.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.voigkampff.data.SearchHistoryDao
import com.example.voigkampff.ui.AnalysisViewModel
import com.example.voigkampff.network.ApiService

// Factory to create AnalysisViewModel with its dependencies
// You need this if AnalysisViewModel has constructor parameters (like searchHistoryDao)
class AnalysisViewModelFactory(
    private val apiService: ApiService,
    private val searchHistoryDao: SearchHistoryDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalysisViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalysisViewModel(apiService, searchHistoryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}