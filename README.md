# PuchUp Camera Tracker

PuchUp Camera Tracker is an Android app that helps limit time spent in selected social apps by earning minutes through pushups. Each counted pushup adds one minute of allowed scrolling time. When time runs out, the app shows a "time is up" message with a fitness illustration.

## How it works
- Starts at 0 minutes; you need to do pushups to earn time.
- Uses the phone camera with ML Kit Pose Detection to count pushups.
- Monitors foreground app usage (YouTube, TikTok, Instagram, Facebook) while the app is running and subtracts minutes.
- Sends a notification and shows a "time is up" screen when minutes reach zero.

## Features in this build
- Live camera preview with pose status and rep counter overlay.
- Calibration mode to help position the camera.
- Onboarding with permission prompts (camera, usage access, notifications).
- Multilingual UI: English, Russian, Ukrainian.

## Requirements
- Android 13+ (API 33)
- Android Studio + Android SDK
- JDK 17 (for Gradle builds)

## Quick Start
1. Open the project in Android Studio.
2. Install SDKs when prompted.
3. Run on a device with Android 13+.

## Permissions
- Camera (pushup counting)
- Usage access (monitor target apps)
- Notifications (time over alerts)

## Notes
- Monitoring works while the app is running (no background service).
- The arm illustration is a placeholder vector and can be replaced later.
