package com.pushupminutes.pose

import android.os.SystemClock
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

class PushupDetector(
    private val onRep: () -> Unit,
    private val onStatus: (Boolean) -> Unit = {}
) {
    private var isDown = false
    private var downFrames = 0
    private var upFrames = 0
    private var lastRepAtMs = 0L

    private val downAngle = 80f
    private val upAngle = 160f
    private val minConfidence = 0.6f
    private val requiredFrames = 3
    private val minRepIntervalMs = 700L

    fun onPose(pose: Pose) {
        val shoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER) ?: run {
            onStatus(false)
            return
        }
        val elbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW) ?: run {
            onStatus(false)
            return
        }
        val wrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST) ?: run {
            onStatus(false)
            return
        }

        val inFrame = hasConfidence(minConfidence, shoulder, elbow, wrist)
        onStatus(inFrame)
        if (!inFrame) return

        val angle = angle(toPoint(shoulder), toPoint(elbow), toPoint(wrist))
        val down = angle < downAngle
        val up = angle > upAngle

        if (down) {
            downFrames += 1
            upFrames = 0
        } else if (up) {
            upFrames += 1
            downFrames = 0
        } else {
            downFrames = 0
            upFrames = 0
        }

        if (!isDown && downFrames >= requiredFrames) {
            isDown = true
            downFrames = 0
        } else if (isDown && upFrames >= requiredFrames) {
            val now = SystemClock.elapsedRealtime()
            if (now - lastRepAtMs >= minRepIntervalMs) {
                lastRepAtMs = now
                onRep()
            }
            isDown = false
            upFrames = 0
        }
    }

    private fun angle(a: PosePoint, b: PosePoint, c: PosePoint): Float {
        val abx = a.x - b.x
        val aby = a.y - b.y
        val cbx = c.x - b.x
        val cby = c.y - b.y
        val dot = abx * cbx + aby * cby
        val ab = kotlin.math.sqrt(abx * abx + aby * aby)
        val cb = kotlin.math.sqrt(cbx * cbx + cby * cby)
        if (ab == 0f || cb == 0f) return 180f
        val cos = (dot / (ab * cb)).coerceIn(-1f, 1f)
        return Math.toDegrees(kotlin.math.acos(cos).toDouble()).toFloat()
    }
}

data class PosePoint(val x: Float, val y: Float)

private fun toPoint(landmark: PoseLandmark): PosePoint {
    return PosePoint(landmark.position.x, landmark.position.y)
}

private fun hasConfidence(minConfidence: Float, vararg landmarks: PoseLandmark): Boolean {
    return landmarks.all { it.inFrameLikelihood >= minConfidence }
}
