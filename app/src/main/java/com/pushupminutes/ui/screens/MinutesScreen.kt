package com.pushupminutes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pushupminutes.R
import com.pushupminutes.data.TargetApps

@Composable
fun MinutesScreen(minutes: Int, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.minutes_available, minutes), style = MaterialTheme.typography.headlineMedium)
        Text(text = stringResource(R.string.target_apps), style = MaterialTheme.typography.titleMedium)
        TargetApps.defaults.forEach { app ->
            Text(text = "- ${app.label}")
        }
        Button(onClick = onBack) {
            Text(text = stringResource(R.string.back))
        }
    }
}
