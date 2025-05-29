package com.example.voigkampff.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voigkampff.data.SearchHistoryDao
import com.example.voigkampff.data.SearchHistoryEntry
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    searchHistoryDao: SearchHistoryDao, // Pass the DAO
    onClearHistory: () -> Unit, // Callback to clear history if needed
    modifier: Modifier = Modifier
) {
    // Collect search history as a Flow and convert to State
    // This will automatically update the UI when the history changes
    val historyItemsState = searchHistoryDao.getAllSearchHistory()
        .collectAsState(initial = emptyList())
    val historyItems = historyItemsState.value

    val coroutineScope = rememberCoroutineScope() // For launching delete operations

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Search History", style = MaterialTheme.typography.headlineSmall)
            if (historyItems.isNotEmpty()) {
                Button(onClick = {
                    // Consider adding a confirmation dialog here
                    coroutineScope.launch {
                        searchHistoryDao.clearAllSearchHistory()
                    }
                    onClearHistory() // Optional: Callback for additional actions
                }) {
                    Text("Clear All")
                }
            }
        }

        if (historyItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No search history yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(historyItems, key = { it.id }) { entry ->
                    HistoryItemCard(
                        entry = entry,
                        onDeleteItem = {
                            coroutineScope.launch {
                                searchHistoryDao.deleteSearchEntryById(entry.id)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    entry: SearchHistoryEntry,
    onDeleteItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "User: ${entry.username}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (entry.isLikelyHuman) "Likely Human: ${100 - entry.botProbabilityPercent}%"
                    else "Likely Bot: ${entry.botProbabilityPercent}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (entry.isLikelyHuman) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Date: ${dateFormat.format(Date(entry.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDeleteItem) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete entry",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}