package com.pushupminutes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pushupminutes.ui.theme.PushupMinutesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PushupMinutesTheme {
                val appViewModel: AppViewModel = viewModel()
                AppRoot(appViewModel)
            }
        }
    }
}
