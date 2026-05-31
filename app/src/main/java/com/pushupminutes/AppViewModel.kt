package com.pushupminutes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pushupminutes.data.MinutesStore
import com.pushupminutes.monitor.UsageMonitor
import com.pushupminutes.notifications.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen {
    ONBOARDING,
    PUSHUPS,
    MINUTES,
    TIMES_UP
}

data class AppUiState(
    val minutes: Int = 0,
    val screen: Screen = Screen.ONBOARDING,
    val encouragement: String = "Keep going"
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val minutesStore = MinutesStore(application)
    private val usageMonitor = UsageMonitor(application)
    private val notificationHelper = NotificationHelper(application)
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()
    private var monitorJob: Job? = null
    private var lastSpendAtMs = 0L
    private var lastBlockAtMs = 0L

    private val encouragements = listOf(
        "Nice rep",
        "Strong work",
        "You got this"
    )

    init {
        viewModelScope.launch {
            minutesStore.minutesFlow.collect { minutes ->
                _uiState.update { state -> state.copy(minutes = minutes) }
            }
        }

        startMonitorLoop()
    }

    fun goTo(screen: Screen) {
        _uiState.update { it.copy(screen = screen) }
    }

    fun addPushup() {
        viewModelScope.launch {
            minutesStore.addMinutes(1)
            val next = encouragements.random()
            _uiState.update { it.copy(encouragement = next) }
        }
    }

    private fun startMonitorLoop() {
        if (monitorJob != null) return
        monitorJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(2000)
                if (!usageMonitor.hasUsageAccess()) continue
                val packageName = usageMonitor.getForegroundPackageName() ?: continue
                val minutes = _uiState.value.minutes

                if (usageMonitor.shouldBlock(packageName, minutes)) {
                    val now = System.currentTimeMillis()
                    if (now - lastBlockAtMs > 30_000L) {
                        lastBlockAtMs = now
                        notificationHelper.showTimeOver()
                        _uiState.update { it.copy(screen = Screen.TIMES_UP) }
                    }
                    continue
                }

                if (usageMonitor.isTargetApp(packageName) && minutes > 0) {
                    val now = System.currentTimeMillis()
                    if (now - lastSpendAtMs >= 60_000L) {
                        if (minutesStore.spendMinute()) {
                            lastSpendAtMs = now
                        }
                    }
                }
            }
        }
    }
}
