package com.mediguide.firstaid.ui.voice

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import com.mediguide.firstaid.R
import kotlin.math.*

/**
 * Custom view for voice visualization with enhanced animations
 */
class VoiceVisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Voice state enum
    enum class VoiceState {
        IDLE, LISTENING, PROCESSING, SPEAKING, EMERGENCY
    }

    private var currentState = VoiceState.IDLE
    private var amplitudeLevel = 0f
    private var animationProgress = 0f

    // Paint objects for different elements
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ripplePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val microphonePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Animation properties
    private var pulseAnimator: ValueAnimator? = null
    private var rippleAnimator: ValueAnimator? = null
    private var waveAnimator: ValueAnimator? = null

    // Colors for different states
    private val idleColor by lazy { ContextCompat.getColor(context, R.color.accent) }
    private val listeningColor by lazy { ContextCompat.getColor(context, R.color.success) }
    private val processingColor by lazy { ContextCompat.getColor(context, R.color.warning) }
    private val speakingColor by lazy { ContextCompat.getColor(context, R.color.primary) }
    private val emergencyColor by lazy { ContextCompat.getColor(context, R.color.emergency_red) }

    init {
        setupPaints()
        startIdleAnimation()
    }

    private fun setupPaints() {
        backgroundPaint.style = Paint.Style.FILL
        
        ripplePaint.style = Paint.Style.STROKE
        ripplePaint.strokeWidth = 4f
        
        microphonePaint.style = Paint.Style.FILL
        
        wavePaint.style = Paint.Style.STROKE
        wavePaint.strokeWidth = 6f
        wavePaint.strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(width, height) / 4f

        // Draw based on current state
        when (currentState) {
            VoiceState.IDLE -> drawIdleState(canvas, centerX, centerY, radius)
            VoiceState.LISTENING -> drawListeningState(canvas, centerX, centerY, radius)
            VoiceState.PROCESSING -> drawProcessingState(canvas, centerX, centerY, radius)
            VoiceState.SPEAKING -> drawSpeakingState(canvas, centerX, centerY, radius)
            VoiceState.EMERGENCY -> drawEmergencyState(canvas, centerX, centerY, radius)
        }
    }

    private fun drawIdleState(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        // Pulsing background circle
        val pulseRadius = radius * (1f + animationProgress * 0.2f)
        val pulseAlpha = (255 * (1f - animationProgress * 0.5f)).toInt()
        
        backgroundPaint.color = idleColor
        backgroundPaint.alpha = pulseAlpha
        canvas.drawCircle(centerX, centerY, pulseRadius, backgroundPaint)
        
        // Main microphone circle
        microphonePaint.color = idleColor
        microphonePaint.alpha = 255
        canvas.drawCircle(centerX, centerY, radius * 0.8f, microphonePaint)
        
        // Microphone icon (simplified)
        drawMicrophoneIcon(canvas, centerX, centerY, radius * 0.4f)
    }

    private fun drawListeningState(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        // Concentric ripples
        for (i in 0..2) {
            val rippleRadius = radius * (0.8f + i * 0.4f + animationProgress * 0.6f)
            val rippleAlpha = (255 * (1f - (animationProgress + i * 0.3f) % 1f)).toInt().coerceAtLeast(0)
            
            ripplePaint.color = listeningColor
            ripplePaint.alpha = rippleAlpha
            canvas.drawCircle(centerX, centerY, rippleRadius, ripplePaint)
        }
        
        // Main microphone circle
        microphonePaint.color = listeningColor
        canvas.drawCircle(centerX, centerY, radius * 0.7f, microphonePaint)
        
        drawMicrophoneIcon(canvas, centerX, centerY, radius * 0.35f)
        
        // Voice amplitude bars
        drawAmplitudeBars(canvas, centerX, centerY, radius)
    }

    private fun drawProcessingState(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        // Rotating processing indicator
        val rotationAngle = animationProgress * 360f

        // Main circle
        microphonePaint.color = processingColor
        canvas.drawCircle(centerX, centerY, radius * 0.7f, microphonePaint)

        drawMicrophoneIcon(canvas, centerX, centerY, radius * 0.35f)

        // Processing dots
        canvas.save()
        canvas.rotate(rotationAngle, centerX, centerY)

        for (i in 0..2) {
            val angle = i * 120f
            val dotX = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * radius
            val dotY = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * radius

            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = processingColor
                alpha = (255 * (0.3f + 0.7f * sin(animationProgress * PI + i * PI / 3).toFloat().absoluteValue)).toInt()
            }

            canvas.drawCircle(dotX, dotY, radius * 0.1f, dotPaint)
        }

        canvas.restore()
    }

    private fun drawSpeakingState(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        // Main circle
        microphonePaint.color = speakingColor
        canvas.drawCircle(centerX, centerY, radius * 0.7f, microphonePaint)

        drawMicrophoneIcon(canvas, centerX, centerY, radius * 0.35f)

        // Speaking waves
        wavePaint.color = speakingColor

        for (i in 1..3) {
            val waveRadius = radius * (0.9f + i * 0.3f)
            val waveAlpha = (255 * (0.8f - i * 0.2f) * sin(animationProgress * PI * 2 + i).toFloat().absoluteValue).toInt()

            wavePaint.alpha = waveAlpha
            canvas.drawCircle(centerX, centerY, waveRadius, wavePaint)
        }
    }

    private fun drawEmergencyState(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        // Urgent pulsing effect
        val pulseScale = 1f + sin(animationProgress * PI * 4).toFloat() * 0.3f
        val pulseRadius = radius * pulseScale

        // Emergency background
        backgroundPaint.color = emergencyColor
        backgroundPaint.alpha = (255 * (0.7f + 0.3f * sin(animationProgress * PI * 8).toFloat().absoluteValue)).toInt()
        canvas.drawCircle(centerX, centerY, pulseRadius, backgroundPaint)

        // Main circle
        microphonePaint.color = emergencyColor
        canvas.drawCircle(centerX, centerY, radius * 0.7f, microphonePaint)

        drawMicrophoneIcon(canvas, centerX, centerY, radius * 0.35f)

        // Emergency alert triangles
        drawEmergencyIndicators(canvas, centerX, centerY, radius)
    }

    private fun drawMicrophoneIcon(canvas: Canvas, centerX: Float, centerY: Float, size: Float) {
        val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        // Simplified microphone shape
        val micRect = RectF(
            centerX - size * 0.3f,
            centerY - size * 0.6f,
            centerX + size * 0.3f,
            centerY - size * 0.1f
        )
        canvas.drawRoundRect(micRect, size * 0.15f, size * 0.15f, iconPaint)

        // Microphone stand
        canvas.drawLine(centerX, centerY - size * 0.1f, centerX, centerY + size * 0.3f, iconPaint.apply { strokeWidth = size * 0.1f })
        canvas.drawLine(centerX - size * 0.2f, centerY + size * 0.3f, centerX + size * 0.2f, centerY + size * 0.3f, iconPaint)
    }

    private fun drawAmplitudeBars(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        val barCount = 8
        val barWidth = 4f
        val maxBarHeight = radius * 0.4f

        wavePaint.color = listeningColor
        wavePaint.strokeWidth = barWidth

        for (i in 0 until barCount) {
            val angle = (i * 360f / barCount) + animationProgress * 180f
            val barHeight = maxBarHeight * (0.2f + 0.8f * amplitudeLevel * sin(animationProgress * PI * 3 + i).toFloat().absoluteValue)

            val startX = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * radius * 1.2f
            val startY = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * radius * 1.2f
            val endX = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * (radius * 1.2f + barHeight)
            val endY = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * (radius * 1.2f + barHeight)

            canvas.drawLine(startX, startY, endX, endY, wavePaint)
        }
    }

    private fun drawEmergencyIndicators(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        val trianglePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.YELLOW
            style = Paint.Style.FILL
        }

        for (i in 0..3) {
            val angle = i * 90f + animationProgress * 180f
            val distance = radius * 1.5f
            val triangleSize = radius * 0.15f

            val triangleX = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * distance
            val triangleY = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * distance

            canvas.save()
            canvas.translate(triangleX, triangleY)
            canvas.rotate(angle + 90f)

            val trianglePath = Path().apply {
                moveTo(0f, -triangleSize)
                lineTo(-triangleSize * 0.8f, triangleSize)
                lineTo(triangleSize * 0.8f, triangleSize)
                close()
            }

            canvas.drawPath(trianglePath, trianglePaint)
            canvas.restore()
        }
    }

    // Public methods for state management
    fun setState(newState: VoiceState) {
        if (currentState != newState) {
            currentState = newState
            startStateAnimation()
        }
    }

    fun setAmplitude(amplitude: Float) {
        amplitudeLevel = amplitude.coerceIn(0f, 1f)
        if (currentState == VoiceState.LISTENING) {
            invalidate()
        }
    }

    private fun startIdleAnimation() {
        stopAllAnimations()

        pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animator ->
                animationProgress = animator.animatedValue as Float
                invalidate()
            }

            start()
        }
    }

    private fun startStateAnimation() {
        stopAllAnimations()

        when (currentState) {
            VoiceState.IDLE -> startIdleAnimation()
            VoiceState.LISTENING -> startListeningAnimation()
            VoiceState.PROCESSING -> startProcessingAnimation()
            VoiceState.SPEAKING -> startSpeakingAnimation()
            VoiceState.EMERGENCY -> startEmergencyAnimation()
        }
    }

    private fun startListeningAnimation() {
        rippleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animator ->
                animationProgress = animator.animatedValue as Float
                invalidate()
            }

            start()
        }
    }

    private fun startProcessingAnimation() {
        waveAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animator ->
                animationProgress = animator.animatedValue as Float
                invalidate()
            }

            start()
        }
    }

    private fun startSpeakingAnimation() {
        waveAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 800
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animator ->
                animationProgress = animator.animatedValue as Float
                invalidate()
            }

            start()
        }
    }

    private fun startEmergencyAnimation() {
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 500
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animator ->
                animationProgress = animator.animatedValue as Float
                invalidate()
            }

            start()
        }
    }

    private fun stopAllAnimations() {
        pulseAnimator?.cancel()
        rippleAnimator?.cancel()
        waveAnimator?.cancel()

        pulseAnimator = null
        rippleAnimator = null
        waveAnimator = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAllAnimations()
    }
}
