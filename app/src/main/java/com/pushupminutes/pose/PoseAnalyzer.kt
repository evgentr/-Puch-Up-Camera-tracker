package com.pushupminutes.pose

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetector

class PoseAnalyzer(
    private val detector: PoseDetector,
    private val pushupDetector: PushupDetector
) {
    fun process(image: InputImage, onError: (Exception) -> Unit = {}) {
        detector.process(image)
            .addOnSuccessListener { pose ->
                pushupDetector.onPose(pose)
            }
            .addOnFailureListener { error ->
                onError(error)
            }
    }
}
