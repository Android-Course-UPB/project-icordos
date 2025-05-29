package com.example.voigkampff // Assuming this is your MainActivity's package

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.voigkampff.ui.theme.VoigKampffTheme // Make sure this theme exists
import com.example.voigkampff.data.SearchHistoryDao
import kotlin.random.Random
import com.example.voigkampff.ui.navigation.AppDestinations
import com.example.voigkampff.ui.AnalysisViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import com.example.voigkampff.ui.AnalysisUiState
import android.widget.Toast
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme // Often used with CircularProgressIndicator
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.voigkampff.data.AppDatabase
import com.example.voigkampff.data.AnalysisViewModelFactory
import com.example.voigkampff.network.RetrofitInstance
import com.example.voigkampff.ui.HistoryScreen // Import your HistoryScreen
import kotlin.text.any
import kotlin.text.isNotBlank
import kotlin.text.toFloat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings // Import Settings icon
import com.example.voigkampff.data.ThemeManager // Import ThemeManager
import com.example.voigkampff.ui.SettingsViewModel
import com.example.voigkampff.ui.SettingsViewModelFactory
import com.example.voigkampff.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize Database and DAO
        val appDatabase = AppDatabase.getDatabase(applicationContext)
        val searchHistoryDao = appDatabase.searchHistoryDao()

        // 2. Get ApiService instance
        val apiService = RetrofitInstance.api // Or however you get your ApiService instance

        // 3. Create ThemeManager
        val themeManager = ThemeManager(applicationContext) // Instantiate ThemeManager

        // 3. Create the ViewModelFactory
        val analysisViewModelFactory = AnalysisViewModelFactory(apiService, searchHistoryDao)
        val settingsViewModelFactory = SettingsViewModelFactory(themeManager) // Instantiate SettingsViewModelFactory

        setContent {
            // Observe the dark mode state from ThemeManager
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false) // Provide an initial value

            VoigKampffTheme(darkTheme = isDarkMode) { // Apply the theme preference
                val analysisViewModel: AnalysisViewModel = viewModel(factory = analysisViewModelFactory)
                val settingsViewModel: SettingsViewModel = viewModel(factory = settingsViewModelFactory) // Get SettingsViewModel

                AppNavigator(
                    navController = rememberNavController(),
                    analysisViewModel = analysisViewModel,
                    searchHistoryDao = searchHistoryDao,
                    settingsViewModel = settingsViewModel // Pass SettingsViewModel
                )
            }
        }
    }
}

// Define items for the Bottom Navigation Bar
sealed class BottomNavItem(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String) {
    data object Landing : BottomNavItem(AppDestinations.LANDING_SCREEN,
        Icons.Filled.Home, "Home")
    data object History : BottomNavItem(AppDestinations.HISTORY_SCREEN,
        Icons.Filled.History, "History")
    data object Settings : BottomNavItem(AppDestinations.SETTINGS_SCREEN,
        Icons.Filled.Settings, "Settings")
}

@Composable
fun AppNavigator(
    navController: NavHostController,
    analysisViewModel: AnalysisViewModel,
    searchHistoryDao: SearchHistoryDao, // Pass the DAO
    settingsViewModel: SettingsViewModel // Add SettingsViewModel parameter
) {
    val context = LocalContext.current
    val analysisUiState by analysisViewModel.uiState.collectAsState()

    // Items for the bottom navigation bar
    val bottomNavItems = listOf(
        BottomNavItem.Landing,
        BottomNavItem.History,
        BottomNavItem.Settings // Add Settings item
    )

    // LaunchedEffect for handling navigation from API calls (e.g., to ResultsScreen)
    LaunchedEffect(analysisUiState) {
        // 'state' here is of type AnalysisUiState (the one imported)
        when (val state = analysisUiState) {
            is AnalysisUiState.Success -> { // Smart cast to AnalysisUiState.Success
                // If AnalysisUiState.Success has a 'response' param, it should be accessible
                if (state.analysis == null) { // Defensive check, though AnalysisResponse itself is non-null in definition
                    Log.e("AppNavigator", "AnalysisUiState.Success but response object is null. This shouldn't happen.")
                    // Potentially show an error or reset
                    Toast.makeText(context, "Error: Received empty success data.", Toast.LENGTH_LONG).show()
                    analysisViewModel.resetState()
                    return@LaunchedEffect
                }

                // If the code reaches here, state.response should be valid IF the definition and imports are correct
                Log.d("AppNavigator", "Navigation to Results: User - ${state.analysis.username}, Prob - ${state.analysis.botProbability}")

                // Ensure 'username' and 'botProbability' exist in your 'AnalysisResponse' data class
                val username = state.analysis.username // Removed ?: "Unknown" for now, assuming username is non-null in AnalysisResponse as per its definition
                val botProbabilityPercentFloat = (state.analysis.botProbability * 100)

                navController.navigate(AppDestinations.resultsRoute(username, botProbabilityPercentFloat.toDouble())) {
                    // ... navigation options ...
                }
                analysisViewModel.resetState()
            }
            is AnalysisUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                Log.e("AppNavigator", "Error State: ${state.message}")
                analysisViewModel.resetState()
            }
            // Handle Loading and Idle if necessary, or just let them pass
            AnalysisUiState.Loading -> { /* Optionally handle loading visuals or logs */ }
            AnalysisUiState.Idle -> { /* Initial state, usually no action */ }
        }
    }

    Scaffold(
        bottomBar = {
            // Only show bottom bar for destinations that are part of it
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            // --- MODIFIED LOGIC FOR showBottomBar ---
            val currentRoute = currentDestination?.route
            val showBottomBar = bottomNavItems.any { it.route == currentRoute } ||
                    currentRoute?.startsWith(AppDestinations.RESULTS_SCREEN.substringBefore("/{")) == true
            // The substringBefore("/{") gets "results_screen" from "results_screen/{username}/{probability}"

            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        // Determine if the current bottom nav item should be marked "selected"
                        // For ResultsScreen, no bottom nav item is "selected" unless you specifically want one to be.
                        // Typically, detail screens like Results don't select a bottom nav item.
                        val isSelected = if (currentRoute?.startsWith(AppDestinations.RESULTS_SCREEN.substringBefore("/{")) == true) {
                            // If on ResultsScreen, decide if any bottom item should appear selected.
                            // For example, if Landing led to Results, you might want Landing to stay selected.
                            // Or, more commonly, no item is selected. Let's go with no item selected for Results.
                            false // Or, e.g., screen.route == BottomNavItem.Landing.route if it's the "parent"
                        } else {
                            currentDestination?.route == screen.route
                        }

                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = isSelected,
                            onClick = {
                                if (screen.route == BottomNavItem.Landing.route) { // Check if the clicked item is "Home"
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true // Save state of the start destination (LandingScreen)
                                            inclusive = false // Keep LandingScreen itself on the stack. Set true if you want to pop it too.
                                            // For typical "Home" button behavior, you want to go *to* Landing, not pop it.
                                        }
                                        launchSingleTop = true
                                        // restoreState = true; // Typically, for Home, you might want it to be fresh or rely on saved state.
                                        // If saveState in popUpTo is true, it will be restored.
                                        // If you always want a "reset" LandingScreen state when clicking Home,
                                        // you might omit saveState and restoreState here.
                                    }
                                } else { // For History and Settings
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true // Restore state for History/Settings
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.LANDING_SCREEN, // Your main start destination
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestinations.LANDING_SCREEN) {
                LandingScreen( // Assuming LandingScreen exists and is imported
                    isLoading = analysisUiState is AnalysisUiState.Loading,
                    onVoigtKampffTestClick = { username ->
                        if (username.isNotBlank()) {
                            analysisViewModel.analyzeUsername(username)
                        } else {
                            Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            composable(
                route = AppDestinations.RESULTS_SCREEN,
                arguments = listOf(
                    navArgument("username") { type = NavType.StringType },
                    navArgument("probability") { type = NavType.FloatType }
                )
            ) { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: "Unknown"
                val probability = backStackEntry.arguments?.getFloat("probability")?.toInt() ?: 0

                ResultsScreen( // Assuming ResultsScreen exists and is imported
                    username = username,
                    botProbabilityPercent = probability,
                    onTestAnotherUserClick = {
                        navController.navigate(AppDestinations.LANDING_SCREEN) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true // Clear the Results screen from backstack
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(AppDestinations.HISTORY_SCREEN) {
                HistoryScreen( // Assuming HistoryScreen exists and is imported
                    searchHistoryDao = searchHistoryDao,
                    onClearHistory = {
                        // Optional: Add a Toast or log message here
                        Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            composable(AppDestinations.SETTINGS_SCREEN) {
                SettingsScreen(settingsViewModel)
            }
        }
    }
}

@Composable
fun LandingScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onVoigtKampffTestClick: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Enter reddit username") },
            singleLine = true,
            enabled = !isLoading, // Disable text field while loading
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onVoigtKampffTestClick(username) },
            enabled = username.isNotBlank() && !isLoading // Button disabled if no username or loading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary // Or another contrasting color
                )
            } else {
                Text("Voigt-Kampff Test")
            }
        }
    }
}