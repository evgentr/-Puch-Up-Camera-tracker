package com.pushupminutes.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pushupminutes.R
import com.pushupminutes.permissions.hasCameraPermission
import com.pushupminutes.permissions.hasNotificationPermission
import com.pushupminutes.permissions.hasUsageAccess
import com.pushupminutes.permissions.openUsageAccessSettings

@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraGranted by remember { mutableStateOf(hasCameraPermission(context)) }
    var usageGranted by remember { mutableStateOf(hasUsageAccess(context)) }
    var notificationsGranted by remember { mutableStateOf(hasNotificationPermission(context)) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        cameraGranted = hasCameraPermission(context)
    }

    val notificationsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        notificationsGranted = hasNotificationPermission(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                cameraGranted = hasCameraPermission(context)
                usageGranted = hasUsageAccess(context)
                notificationsGranted = hasNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.onboarding_title), style = MaterialTheme.typography.headlineMedium)
        Text(text = stringResource(R.string.onboarding_body), style = MaterialTheme.typography.bodyLarge)

        Text(
            text = stringResource(
                if (cameraGranted) R.string.permission_camera_granted else R.string.permission_camera_needed
            ),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        Button(onClick = { cameraLauncher.launch(Manifest.permission.CAMERA) }) {
            Text(text = stringResource(R.string.permission_camera_button))
        }

        Text(
            text = stringResource(
                if (usageGranted) R.string.permission_usage_granted else R.string.permission_usage_needed
            ),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        Button(onClick = { openUsageAccessSettings(context) }) {
            Text(text = stringResource(R.string.permission_usage_button))
        }

        Text(
            text = stringResource(
                if (notificationsGranted) R.string.permission_notifications_granted else R.string.permission_notifications_needed
            ),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        Button(onClick = { notificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
            Text(text = stringResource(R.string.permission_notifications_button))
        }

        Button(
            onClick = onContinue,
            enabled = cameraGranted && usageGranted,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(text = stringResource(R.string.onboarding_cta))
        }
    }
}
