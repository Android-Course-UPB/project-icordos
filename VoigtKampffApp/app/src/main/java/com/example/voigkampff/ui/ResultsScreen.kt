// In a new file, e.g., ui/ResultsScreen.kt or within MainActivity.kt if simple

package com.example.voigkampff // Or com.example.voigkampff.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voigkampff.ui.theme.VoigKampffTheme

@Composable
fun ResultsScreen(
    modifier: Modifier = Modifier,
    username: String,
    botProbabilityPercent: Int, // Expecting an Int percentage (0-100)
    onTestAnotherUserClick: () -> Unit
) {
    val displayText = if (botProbabilityPercent < 50) {
        val humanProbabilityPercent = 100 - botProbabilityPercent
        "User '$username' is likely a human with a $humanProbabilityPercent% probability"
    } else {
        "User '$username' is likely a bot with a $botProbabilityPercent% probability"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = displayText,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Button(onClick = onTestAnotherUserClick) {
            Text("Test Another User")
        }
    }
}


/*

@Preview(showBackground = true)
@Composable
fun ResultsScreenPreview() {
    VoigKampffTheme {
        ResultsScreen(
            modifier: Modifier = Modifier,
            username = "SomeRedditor",
            botProbabilityPercent = "44", // Expecting an Int percentage (0-100)
            onTestAnotherUserClick: () -> Unit
        )
    }
}*/