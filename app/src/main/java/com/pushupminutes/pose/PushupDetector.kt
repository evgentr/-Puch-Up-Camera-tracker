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

    private val downAngle = 100f
    private val upAngle = 150f
    private val minConfidence = 0.4f
    private val requiredFrames = 2
    private val minRepIntervalMs = 600L

    fun onPose(pose: Pose) {
        val arm = pickBestArm(pose)
        if (arm == null) {
            onStatus(false)
            return
        }

        val inFrame = hasConfidence(minConfidence, arm.shoulder, arm.elbow, arm.wrist)
        onStatus(inFrame)
        if (!inFrame) return

        val angle = angle(toPoint(arm.shoulder), toPoint(arm.elbow), toPoint(arm.wrist))
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

private data class ArmLandmarks(
    val shoulder: PoseLandmark,
    val elbow: PoseLandmark,
    val wrist: PoseLandmark
)

private fun toPoint(landmark: PoseLandmark): PosePoint {
    return PosePoint(landmark.position.x, landmark.position.y)
}

private fun hasConfidence(minConfidence: Float, vararg landmarks: PoseLandmark): Boolean {
    return landmarks.all { it.inFrameLikelihood >= minConfidence }
}

private fun pickBestArm(pose: Pose): ArmLandmarks? {
    val left = armOrNull(
        pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER),
        pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW),
        pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
    )
    val right = armOrNull(
        pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER),
        pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW),
        pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
    )

    if (left == null && right == null) return null
    if (left == null) return right
    if (right == null) return left

    val leftScore = left.shoulder.inFrameLikelihood + left.elbow.inFrameLikelihood + left.wrist.inFrameLikelihood
    val rightScore = right.shoulder.inFrameLikelihood + right.elbow.inFrameLikelihood + right.wrist.inFrameLikelihood
    return if (rightScore > leftScore) right else left
}

private fun armOrNull(
    shoulder: PoseLandmark?,
    elbow: PoseLandmark?,
    wrist: PoseLandmark?
): ArmLandmarks? {
    if (shoulder == null || elbow == null || wrist == null) return null
    return ArmLandmarks(shoulder, elbow, wrist)
}
