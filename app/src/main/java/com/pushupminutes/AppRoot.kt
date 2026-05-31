package com.pushupminutes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.pushupminutes.ui.screens.MinutesScreen
import com.pushupminutes.ui.screens.OnboardingScreen
import com.pushupminutes.ui.screens.PushupScreen
import com.pushupminutes.ui.screens.TimesUpScreen

@Composable
fun AppRoot(viewModel: AppViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState.screen) {
        Screen.ONBOARDING -> OnboardingScreen(
            onContinue = { viewModel.goTo(Screen.PUSHUPS) }
        )
        Screen.PUSHUPS -> PushupScreen(
            minutes = uiState.minutes,
            encouragement = uiState.encouragement,
            onAddPushup = { viewModel.addPushup() },
            onShowMinutes = { viewModel.goTo(Screen.MINUTES) }
        )
        Screen.MINUTES -> MinutesScreen(
            minutes = uiState.minutes,
            onBack = { viewModel.goTo(Screen.PUSHUPS) }
        )
        Screen.TIMES_UP -> TimesUpScreen(
            onBackToPushups = { viewModel.goTo(Screen.PUSHUPS) }
        )
    }
}
