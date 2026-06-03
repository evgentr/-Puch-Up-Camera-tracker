package com.pushupminutes.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pushupminutes.R
import com.pushupminutes.permissions.hasCameraPermission
import com.pushupminutes.permissions.openAppSettings

@Composable
fun PushupScreen(
    minutes: Int,
    encouragement: String,
    languageTag: String,
    onToggleLanguage: () -> Unit,
    onAddPushup: () -> Unit,
    onShowMinutes: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraGranted by remember { mutableStateOf(hasCameraPermission(context)) }
    var repCount by remember { mutableStateOf(0) }
    var inFrame by remember { mutableStateOf(false) }
    var showCalibration by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(true) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        cameraGranted = hasCameraPermission(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                cameraGranted = hasCameraPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.pushup_title), style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onToggleLanguage) {
                Text(
                    text = if (languageTag == "en") stringResource(R.string.language_ru) else stringResource(R.string.language_en)
                )
            }
        }
        Text(text = "${stringResource(R.string.minutes_title)}: $minutes", style = MaterialTheme.typography.titleLarge)
        Text(text = encouragement, style = MaterialTheme.typography.bodyLarge)

        if (cameraGranted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                PushupCamera(
                    onRep = {
                        repCount += 1
                        onAddPushup()
                    },
                    onPoseStatus = { inFrame = it },
                    useFrontCamera = useFrontCamera,
                    modifier = Modifier.matchParentSize()
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(Color(0x99000000))
                        .padding(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.pushup_reps, repCount),
                        color = Color.White
                    )
                    Text(
                        text = stringResource(
                            if (inFrame) R.string.pushup_status_ok else R.string.pushup_status_adjust
                        ),
                        color = Color.White
                    )
                }

                if (showCalibration) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .background(Color(0x99000000))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.calibration_title),
                            color = Color.White
                        )
                        Text(
                            text = stringResource(R.string.calibration_body),
                            color = Color.White
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(onClick = { showCalibration = !showCalibration }) {
                    Text(
                        text = stringResource(
                            if (showCalibration) R.string.calibration_hide else R.string.calibration_show
                        )
                    )
                }
                Button(onClick = { useFrontCamera = !useFrontCamera }) {
                    Text(text = stringResource(R.string.camera_switch))
                }
            }
        } else {
            Text(text = stringResource(R.string.pushup_camera_needed))
            Button(onClick = { cameraLauncher.launch(Manifest.permission.CAMERA) }) {
                Text(text = stringResource(R.string.pushup_camera_button))
            }
            Button(onClick = { openAppSettings(context) }) {
                Text(text = stringResource(R.string.pushup_camera_settings))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onShowMinutes, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.minutes_title))
        }
    }
}
