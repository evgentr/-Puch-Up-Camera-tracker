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

    private val downAngle = 120f
    private val upAngle = 150f
    private val minConfidence = 0.3f
    private val requiredFrames = 2
    private val minRepIntervalMs = 500L
    private val minBodyAngle = 150f
    private val downArmRatio = 0.82f
    private val upArmRatio = 0.95f

    fun onPose(pose: Pose) {
        val arm = pickBestSide(pose)
        if (arm == null) {
            onStatus(false)
            return
        }

        val inFrame = hasConfidence(
            minConfidence,
            arm.shoulder,
            arm.elbow,
            arm.wrist,
            arm.hip,
            arm.ankle
        )
        onStatus(inFrame)
        if (!inFrame) return

        val elbowAngle = angle(toPoint(arm.shoulder), toPoint(arm.elbow), toPoint(arm.wrist))
        val bodyAngle = angle(toPoint(arm.shoulder), toPoint(arm.hip), toPoint(arm.ankle))
        val armLength = distance(toPoint(arm.shoulder), toPoint(arm.elbow)) +
            distance(toPoint(arm.elbow), toPoint(arm.wrist))
        val shoulderToWrist = distance(toPoint(arm.shoulder), toPoint(arm.wrist))
        val armRatio = if (armLength == 0f) 1f else shoulderToWrist / armLength

        if (bodyAngle < minBodyAngle) return

        val down = elbowAngle < downAngle && armRatio < downArmRatio
        val up = elbowAngle > upAngle && armRatio > upArmRatio

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

    private fun distance(a: PosePoint, b: PosePoint): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
}

data class PosePoint(val x: Float, val y: Float)

private data class SideLandmarks(
    val shoulder: PoseLandmark,
    val elbow: PoseLandmark,
    val wrist: PoseLandmark,
    val hip: PoseLandmark,
    val ankle: PoseLandmark
)

private fun toPoint(landmark: PoseLandmark): PosePoint {
    return PosePoint(landmark.position.x, landmark.position.y)
}

private fun hasConfidence(minConfidence: Float, vararg landmarks: PoseLandmark): Boolean {
    return landmarks.all { it.inFrameLikelihood >= minConfidence }
}

private fun pickBestSide(pose: Pose): SideLandmarks? {
    val left = sideOrNull(
        pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER),
        pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW),
        pose.getPoseLandmark(PoseLandmark.LEFT_WRIST),
        pose.getPoseLandmark(PoseLandmark.LEFT_HIP),
        pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
    )
    val right = sideOrNull(
        pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER),
        pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW),
        pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST),
        pose.getPoseLandmark(PoseLandmark.RIGHT_HIP),
        pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
    )

    if (left == null && right == null) return null
    if (left == null) return right
    if (right == null) return left

    val leftScore = left.shoulder.inFrameLikelihood + left.elbow.inFrameLikelihood + left.wrist.inFrameLikelihood +
        left.hip.inFrameLikelihood + left.ankle.inFrameLikelihood
    val rightScore = right.shoulder.inFrameLikelihood + right.elbow.inFrameLikelihood + right.wrist.inFrameLikelihood +
        right.hip.inFrameLikelihood + right.ankle.inFrameLikelihood
    return if (rightScore > leftScore) right else left
}

private fun sideOrNull(
    shoulder: PoseLandmark?,
    elbow: PoseLandmark?,
    wrist: PoseLandmark?,
    hip: PoseLandmark?,
    ankle: PoseLandmark?
): SideLandmarks? {
    if (shoulder == null || elbow == null || wrist == null || hip == null || ankle == null) return null
    return SideLandmarks(shoulder, elbow, wrist, hip, ankle)
}
