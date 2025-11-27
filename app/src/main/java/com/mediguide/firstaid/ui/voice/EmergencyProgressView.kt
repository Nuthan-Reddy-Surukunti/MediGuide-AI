package com.mediguide.firstaid.ui.voice

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.mediguide.firstaid.R
import kotlin.math.sin

/**
 * Custom progress view for emergency procedures
 */
class EmergencyProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class EmergencyStep(
        val id: Int,
        val title: String,
        val description: String,
        val isTimeSensitive: Boolean = false,
        val duration: Long = 0L,
        val isCompleted: Boolean = false,
        val isCurrent: Boolean = false
    )

    private var steps = listOf<EmergencyStep>()
    private var currentStepIndex = 0
    private var progress = 0f

    // Paint objects
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stepPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Colors
    private val completedColor by lazy { ContextCompat.getColor(context, R.color.emergency_step_complete) }
    private val currentColor by lazy { ContextCompat.getColor(context, R.color.emergency_step_current) }
    private val pendingColor by lazy { ContextCompat.getColor(context, R.color.emergency_step_pending) }
    private val backgroundColor by lazy { ContextCompat.getColor(context, R.color.emergency_surface) }
    private val textColor by lazy { ContextCompat.getColor(context, R.color.emergency_on_surface) }

    init {
        setupPaints()
    }

    private fun setupPaints() {
        backgroundPaint.apply {
            style = Paint.Style.FILL
            color = backgroundColor
        }

        progressPaint.apply {
            style = Paint.Style.FILL
            strokeCap = Paint.Cap.ROUND
        }

        textPaint.apply {
            color = textColor
            textSize = 32f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        stepPaint.apply {
            style = Paint.Style.FILL
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (steps.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2f

        // Draw progress background
        val progressHeight = 12f
        val progressY = centerY - progressHeight / 2f

        backgroundPaint.color = pendingColor
        canvas.drawRoundRect(
            40f, progressY,
            width - 40f, progressY + progressHeight,
            progressHeight / 2f, progressHeight / 2f,
            backgroundPaint
        )

        // Draw progress fill
        val progressWidth = (width - 80f) * (progress / 100f)
        progressPaint.color = completedColor
        canvas.drawRoundRect(
            40f, progressY,
            40f + progressWidth, progressY + progressHeight,
            progressHeight / 2f, progressHeight / 2f,
            progressPaint
        )

        // Draw step indicators
        drawStepIndicators(canvas, width, centerY)

        // Draw current step info
        drawCurrentStepInfo(canvas, width, height)
    }

    private fun drawStepIndicators(canvas: Canvas, width: Float, centerY: Float) {
        if (steps.isEmpty()) return

        val stepSpacing = (width - 80f) / (steps.size - 1).coerceAtLeast(1)
        val circleRadius = 20f

        steps.forEachIndexed { index, step ->
            val x = 40f + index * stepSpacing
            val y = centerY

            // Determine step color
            val color = when {
                step.isCompleted -> completedColor
                step.isCurrent -> currentColor
                else -> pendingColor
            }

            // Draw step circle
            stepPaint.color = color
            canvas.drawCircle(x, y, circleRadius, stepPaint)

            // Draw step number or checkmark
            if (step.isCompleted) {
                // Draw checkmark
                val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                checkPaint.color = Color.WHITE
                checkPaint.strokeWidth = 4f
                checkPaint.style = Paint.Style.STROKE
                checkPaint.strokeCap = Paint.Cap.ROUND

                val checkPath = Path().apply {
                    moveTo(x - 8f, y)
                    lineTo(x - 2f, y + 6f)
                    lineTo(x + 8f, y - 6f)
                }
                canvas.drawPath(checkPath, checkPaint)
            } else {
                // Draw step number
                val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                numberPaint.color = Color.WHITE
                numberPaint.textSize = 24f
                numberPaint.typeface = Typeface.DEFAULT_BOLD
                numberPaint.textAlign = Paint.Align.CENTER

                val textY = y + (numberPaint.textSize / 3f)
                canvas.drawText("${index + 1}", x, textY, numberPaint)
            }
        }
    }

    private fun drawCurrentStepInfo(canvas: Canvas, width: Float, height: Float) {
        if (currentStepIndex < steps.size) {
            val currentStep = steps[currentStepIndex]
            val textY = height - 40f

            // Draw current step title
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                textSize = 28f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(currentStep.title, width / 2f, textY - 40f, titlePaint)

            // Draw progress percentage
            val progressTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                textSize = 48f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("${progress.toInt()}%", width / 2f, textY, progressTextPaint)
        }
    }

    // Public methods
    fun setSteps(newSteps: List<EmergencyStep>) {
        steps = newSteps
        currentStepIndex = steps.indexOfFirst { it.isCurrent }.coerceAtLeast(0)
        updateProgress()
        invalidate()
    }

    fun setCurrentStep(stepIndex: Int) {
        if (stepIndex in steps.indices) {
            // Update step states
            steps = steps.mapIndexed { index, step ->
                step.copy(
                    isCompleted = index < stepIndex,
                    isCurrent = index == stepIndex
                )
            }
            currentStepIndex = stepIndex
            updateProgress()
            invalidate()
        }
    }

    fun completeCurrentStep() {
        if (currentStepIndex < steps.size) {
            steps = steps.mapIndexed { index, step ->
                if (index == currentStepIndex) {
                    step.copy(isCompleted = true, isCurrent = false)
                } else if (index == currentStepIndex + 1) {
                    step.copy(isCurrent = true)
                } else {
                    step
                }
            }
            currentStepIndex++
            updateProgress()
            invalidate()
        }
    }

    fun setProgress(newProgress: Float) {
        progress = newProgress.coerceIn(0f, 100f)
        invalidate()
    }

    private fun updateProgress() {
        val completedSteps = steps.count { it.isCompleted }
        progress = if (steps.isNotEmpty()) {
            (completedSteps.toFloat() / steps.size.toFloat()) * 100f
        } else {
            0f
        }
    }

    fun getCurrentStep(): EmergencyStep? {
        return if (currentStepIndex < steps.size) steps[currentStepIndex] else null
    }

    fun isCompleted(): Boolean {
        return steps.all { it.isCompleted }
    }
}
