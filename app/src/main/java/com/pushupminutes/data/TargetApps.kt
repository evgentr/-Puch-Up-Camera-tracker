package com.pushupminutes.data

data class TargetApp(
    val packageName: String,
    val label: String
)

object TargetApps {
    val defaults = listOf(
        TargetApp("com.google.android.youtube", "YouTube"),
        TargetApp("com.zhiliaoapp.musically", "TikTok"),
        TargetApp("com.instagram.android", "Instagram"),
        TargetApp("com.facebook.katana", "Facebook")
    )
}
