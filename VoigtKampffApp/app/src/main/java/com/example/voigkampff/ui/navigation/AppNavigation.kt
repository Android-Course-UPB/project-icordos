// You can put this in a new file, e.g., ui/navigation/AppNavigation.kt
// or at the top of your MainActivity.kt if you prefer for a small app.

package com.example.voigkampff.ui.navigation // Or your chosen package


object AppDestinations {
    const val LANDING_SCREEN = "landing_screen"
    // Route for results screen, taking username and probability (as a Double string)
    const val RESULTS_SCREEN = "results_screen/{username}/{probability}"
    const val HISTORY_SCREEN = "history_screen" // New destination
    const val SETTINGS_SCREEN = "settings_screen" // New destination

    // Helper function to create the route with arguments
    fun resultsRoute(username: String, probability: Double): String {
        // Ensure probability is formatted in a way that can be parsed back to Double
        return "results_screen/$username/$probability"
    }
}
