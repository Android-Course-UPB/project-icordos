package com.example.voigkampff.ui // Or wherever your ViewModels/Factories are located

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.voigkampff.data.ThemeManager

class SettingsViewModelFactory(private val themeManager: ThemeManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(themeManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}