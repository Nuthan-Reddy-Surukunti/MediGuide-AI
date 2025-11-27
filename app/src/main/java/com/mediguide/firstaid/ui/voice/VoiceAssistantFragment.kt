package com.mediguide.firstaid.ui.voice

// UI fragment for interacting with the voice assistant (listen, speak, emergency shortcuts)

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.mediguide.firstaid.databinding.FragmentVoiceAssistantBinding
import com.mediguide.firstaid.voice.VoiceAssistantState
import com.mediguide.firstaid.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * Fragment for Voice Assistant Interface
 */
class VoiceAssistantFragment : Fragment() {

    private var _binding: FragmentVoiceAssistantBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VoiceAssistantViewModel by viewModels()
 
    private var hasRequestedPermissions = false
 
    // Permission launcher for runtime permission requests
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            Log.d("VoiceAssistantFragment", "All permissions granted, initializing voice assistant")
            Toast.makeText(requireContext(), "Permissions granted", Toast.LENGTH_SHORT).show()
            hasRequestedPermissions = false // Reset flag
            viewModel.initialize()
        } else {
            val deniedPermissions = permissions.filter { !it.value }.keys
            Log.w("VoiceAssistantFragment", "Permissions denied: $deniedPermissions")
            hasRequestedPermissions = true // Mark that we've requested (and user denied)
            showPermissionDeniedDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoiceAssistantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
        checkAndRequestPermissions()
    }

    override fun onResume() {
        super.onResume()
        // Re-check permissions when fragment resumes (e.g., returning from Settings)
        checkAndRequestPermissions()
    }

    private fun setupUI() {
        // Main emergency access button (toggle expandable options)
        binding.btnEmergencyAccess.setOnClickListener {
            toggleEmergencyOptions()
        }

        // Individual emergency action buttons (shown when expanded)
        binding.btnEmergencyCpr.setOnClickListener {
            viewModel.startCPRGuidance()
            collapseEmergencyOptions()
        }

        binding.btnEmergencyChoking.setOnClickListener {
            viewModel.startChokingGuidance()
            collapseEmergencyOptions()
        }

        binding.btnEmergencyBleeding.setOnClickListener {
            viewModel.startBleedingGuidance()
            collapseEmergencyOptions()
        }

        binding.btnEmergencyBurns.setOnClickListener {
            viewModel.startBurnsGuidance()
            collapseEmergencyOptions()
        }

        // Enhanced microphone button with context-aware functionality
        setupEnhancedMicrophoneButton()

        // Enhanced Emergency Mode Buttons
        binding.btnExitEmergency.setOnClickListener {
            viewModel.exitEmergencyMode()
        }

        binding.btnStepComplete.setOnClickListener {
            completeCurrentEmergencyStep()
        }

        binding.btnRepeatStep.setOnClickListener {
            repeatCurrentEmergencyStep()
        }

        binding.btnCallEmergency.setOnClickListener {
            callEmergencyServices()
        }






    }

    private fun observeViewModel() {
        // Assistant state
        viewModel.assistantState.observe(viewLifecycleOwner) { state ->
            updateUIForState(state)
        }

        // Recognized text
        viewModel.recognizedText.observe(viewLifecycleOwner) { text ->
            binding.tvRecognizedText.text = text
        }

        // AI Response
        viewModel.currentResponse.observe(viewLifecycleOwner) { response ->
            response?.let {
                val formattedText = formatAIResponse(it.text ?: "No response available")
                binding.tvAiResponse.text = formattedText
            } ?: run {
                binding.tvAiResponse.text = "AI features unavailable - using offline mode"
            }
        }

        // Error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
        }

        // AI Status monitoring
        viewModel.isAIOnline.observe(viewLifecycleOwner) { isOnline ->
            updateAIStatusIndicator(isOnline)
        }

        // Initialization status
        viewModel.isInitialized.observe(viewLifecycleOwner) { initialized ->
            if (initialized) {
                binding.microphoneButton.isEnabled = true
                binding.statusText.text = "Tap microphone for voice guidance"
            } else {
                binding.microphoneButton.isEnabled = false
                binding.statusText.text = "Initializing voice system..."
            }
        }

        // Emergency mode
        viewModel.showEmergencyMode.observe(viewLifecycleOwner) { showEmergency ->
            if (showEmergency) {
                binding.emergencyOverlay.visibility = View.VISIBLE
                binding.normalModeLayout.visibility = View.GONE
                setupEmergencyMode()
            } else {
                binding.emergencyOverlay.visibility = View.GONE
                binding.normalModeLayout.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Update the AI status indicator based on online/offline state with enhanced animations
     */
    private fun updateAIStatusIndicator(isOnline: Boolean) {
        if (isOnline) {
            // AI is online - show green indicator with animation
            binding.aiStatusIndicator.setBackgroundResource(R.drawable.status_indicator_online)
            binding.aiStatusText.text = "AI Status"
            binding.aiStatusDetail.text = "Online & Ready"
            binding.aiStatusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.success))
            binding.aiStatusDetail.setTextColor(ContextCompat.getColor(requireContext(), R.color.success))

            // Add pulsing animation for online status
            startStatusPulseAnimation(true)
        } else {
            // AI is offline - show red indicator
            binding.aiStatusIndicator.setBackgroundResource(R.drawable.status_indicator_offline)
            binding.aiStatusText.text = "AI Status"
            binding.aiStatusDetail.text = "Offline Mode"
            binding.aiStatusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.error))
            binding.aiStatusDetail.setTextColor(ContextCompat.getColor(requireContext(), R.color.error))

            // Stop pulsing animation for offline
            stopStatusPulseAnimation()
        }
    }

    private fun startStatusPulseAnimation(isOnline: Boolean) {
        // Pulse animation using the status indicator itself
        val pulseAnimator = android.animation.ObjectAnimator.ofFloat(binding.aiStatusIndicator, "alpha", 0.5f, 1.0f).apply {
            duration = 1000
            repeatCount = android.animation.ValueAnimator.INFINITE
            repeatMode = android.animation.ValueAnimator.REVERSE
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
        }
        pulseAnimator.start()
    }

    private fun stopStatusPulseAnimation() {
        binding.aiStatusIndicator.clearAnimation()
        binding.aiStatusIndicator.alpha = 1.0f
    }



    /**
     * Copy response to clipboard
     */
    private fun copyResponseToClipboard() {
        val responseText = binding.tvAiResponse.text.toString()
        if (responseText.isNotBlank()) {
            val clipboardManager = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("First Aid Response", responseText)
            clipboardManager.setPrimaryClip(clip)

            Toast.makeText(requireContext(), "Response copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Setup enhanced microphone button with context-aware functionality
     */
    private fun setupEnhancedMicrophoneButton() {
        // Single click: Start/Stop voice recognition
        binding.microphoneButton.setOnClickListener {
            handleMicrophoneClick()
        }

        // Long click: Clear conversation
        binding.microphoneButton.setOnLongClickListener {
            handleMicrophoneLongPress()
            true
        }
    }

    /**
     * Handle microphone button click with context-aware behavior
     */
    private fun handleMicrophoneClick() {
        when (currentMicState) {
            MicState.IDLE -> {
                // Start voice recognition
                viewModel.startListening()
                updateMicrophoneState(MicState.LISTENING)
            }
            MicState.LISTENING -> {
                // Stop listening
                viewModel.stopListening()
                updateMicrophoneState(MicState.IDLE)
            }
            MicState.SPEAKING -> {
                // Stop speaking
                viewModel.stopSpeaking()
                updateMicrophoneState(MicState.IDLE)
            }
            MicState.PROCESSING -> {
                // Cancel processing if supported by ViewModel
                // viewModel.cancelProcessing()
                updateMicrophoneState(MicState.IDLE)
            }
        }
    }

    /**
     * Handle microphone button long press for clearing conversation
     */
    private fun handleMicrophoneLongPress() {
        // Haptic feedback for long press
        binding.microphoneButton.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)

        // Show confirmation dialog
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Clear Conversation")
            .setMessage("Are you sure you want to clear the entire conversation?")
            .setPositiveButton("Clear") { _, _ ->
                clearConversation()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Clear the conversation
     */
    private fun clearConversation() {
        viewModel.clearConversation()
        binding.tvAiResponse.text = "How can I help you with training?"
        binding.tvRecognizedText.text = "Waiting for input..."

        Toast.makeText(requireContext(), "Conversation cleared", Toast.LENGTH_SHORT).show()
    }

    // Microphone states for context-aware button behavior
    enum class MicState {
        IDLE, LISTENING, PROCESSING, SPEAKING
    }

    private var currentMicState = MicState.IDLE

    /**
     * Update microphone button appearance based on current state
     */
    private fun updateMicrophoneState(newState: MicState) {
        currentMicState = newState

        when (newState) {
            MicState.IDLE -> {
                binding.microphoneButton.setImageResource(R.drawable.voice_mic_idle)
                binding.statusText.text = "Tap microphone to start"
                binding.microphoneButton.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.accent)
            }
            MicState.LISTENING -> {
                binding.microphoneButton.setImageResource(R.drawable.voice_mic_listening)
                binding.statusText.text = "Listening... Tap to stop"
                binding.microphoneButton.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.error)
            }
            MicState.PROCESSING -> {
                binding.microphoneButton.setImageResource(R.drawable.voice_mic_processing)
                binding.statusText.text = "Processing... Tap to cancel"
                binding.microphoneButton.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.warning)
            }
            MicState.SPEAKING -> {
                binding.microphoneButton.setImageResource(R.drawable.voice_mic_speaking)
                binding.statusText.text = "Speaking... Tap to stop"
                binding.microphoneButton.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.info)
            }
        }
    }





    /**
     * Toggle emergency options visibility with smooth animation
     */
    private fun toggleEmergencyOptions() {
        val isCurrentlyVisible = binding.emergencyOptionsContainer.visibility == View.VISIBLE

        if (isCurrentlyVisible) {
            collapseEmergencyOptions()
        } else {
            expandEmergencyOptions()
        }
    }

    /**
     * Expand emergency options with animation
     */
    private fun expandEmergencyOptions() {
        binding.emergencyOptionsContainer.visibility = View.VISIBLE

        // Update main button text to show it's expanded
        binding.btnEmergencyAccess.text = "🚨 Close Emergency Options"

        // Add smooth slide-down animation
        val slideDown = android.view.animation.AnimationUtils.loadAnimation(requireContext(), android.R.anim.slide_in_left)
        binding.emergencyOptionsContainer.startAnimation(slideDown)

        // Provide haptic feedback
        binding.btnEmergencyAccess.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
    }

    /**
     * Collapse emergency options with animation
     */
    private fun collapseEmergencyOptions() {
        // Update main button text to show it's collapsed
        binding.btnEmergencyAccess.text = "🚨 Emergency Quick Actions"

        // Add smooth slide-up animation
        val slideUp = android.view.animation.AnimationUtils.loadAnimation(requireContext(), android.R.anim.slide_out_right)
        slideUp.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                binding.emergencyOptionsContainer.visibility = View.GONE
            }
        })

        binding.emergencyOptionsContainer.startAnimation(slideUp)
    }

    private fun updateUIForState(state: VoiceAssistantState) {
        when (state) {
            is VoiceAssistantState.Idle -> {
                binding.microphoneButton.isEnabled = true
                binding.statusText.text = "Tap to speak"
                binding.microphoneButton.setImageResource(R.drawable.voice_mic_idle)
                binding.microphoneButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.accent)
                // Hide old animations
                binding.listeningAnimation.visibility = View.GONE
                binding.processingAnimation.visibility = View.GONE
                binding.speakingAnimation.visibility = View.GONE
            }
            is VoiceAssistantState.Listening -> {
                binding.statusText.text = "Listening..."
                binding.microphoneButton.setImageResource(R.drawable.voice_mic_listening)
                binding.microphoneButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.success)
                // Hide old animations
                binding.listeningAnimation.visibility = View.GONE
                binding.processingAnimation.visibility = View.GONE
                binding.speakingAnimation.visibility = View.GONE
            }
            is VoiceAssistantState.Processing -> {
                binding.statusText.text = "Processing..."
                binding.microphoneButton.setImageResource(R.drawable.voice_mic_processing)
                binding.microphoneButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.warning)
                // Hide old animations
                binding.listeningAnimation.visibility = View.GONE
                binding.processingAnimation.visibility = View.GONE
                binding.speakingAnimation.visibility = View.GONE
            }
            is VoiceAssistantState.Speaking -> {
                binding.statusText.text = "Speaking..."
                binding.microphoneButton.setImageResource(R.drawable.voice_mic_speaking)
                binding.microphoneButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primary)
                // Hide old animations
                binding.listeningAnimation.visibility = View.GONE
                binding.processingAnimation.visibility = View.GONE
                binding.speakingAnimation.visibility = View.GONE
            }
            is VoiceAssistantState.Connecting -> {
                binding.microphoneButton.isEnabled = false
                binding.statusText.text = "Connecting to Live AI..."
                binding.microphoneButton.setImageResource(R.drawable.voice_mic_processing)
                binding.microphoneButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.warning)
                // Hide old animations
                binding.listeningAnimation.visibility = View.GONE
                binding.processingAnimation.visibility = View.GONE
                binding.speakingAnimation.visibility = View.GONE
            }
            is VoiceAssistantState.LiveConversation -> {
                binding.microphoneButton.isEnabled = true
                binding.statusText.text = "🚨 LIVE Emergency Assistant Active"
                binding.microphoneButton.setImageResource(R.drawable.voice_mic_emergency)
                binding.microphoneButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.emergency_red)
                // Hide old animations
                binding.listeningAnimation.visibility = View.GONE
                binding.processingAnimation.visibility = View.GONE
                binding.speakingAnimation.visibility = View.GONE
            }
            is VoiceAssistantState.Error -> {
                binding.statusText.text = "Error: ${state.message}"
                binding.microphoneButton.setImageResource(R.drawable.voice_mic_idle)
                binding.microphoneButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.accent)
                // Hide old animations
                binding.listeningAnimation.visibility = View.GONE
                binding.processingAnimation.visibility = View.GONE
                binding.speakingAnimation.visibility = View.GONE
            }
        }
    }

    /**
     * Emergency Mode Management Methods
     */
    private fun setupEmergencyMode() {
        // Setup emergency steps based on the current emergency type
        val emergencySteps = when {
            // For now, use CPR as default - this would be determined by viewModel
            else -> createCPRSteps()
        }

        binding.emergencyProgress.setSteps(emergencySteps)
        binding.tvEmergencyType.text = "CPR GUIDANCE"
        updateCurrentEmergencyStep()
    }

    private fun createCPRSteps(): List<EmergencyProgressView.EmergencyStep> {
        return listOf(
            EmergencyProgressView.EmergencyStep(
                id = 1,
                title = "Check Responsiveness",
                description = "Tap shoulders and shout 'Are you okay?'",
                isTimeSensitive = false,
                isCurrent = true
            ),
            EmergencyProgressView.EmergencyStep(
                id = 2,
                title = "Call for Help",
                description = "Call 112 or have someone else do it",
                isTimeSensitive = false
            ),
            EmergencyProgressView.EmergencyStep(
                id = 3,
                title = "Position Hands",
                description = "Place heel of hand on center of chest",
                isTimeSensitive = false
            ),
            EmergencyProgressView.EmergencyStep(
                id = 4,
                title = "Start Compressions",
                description = "Push hard and fast at least 2 inches deep",
                isTimeSensitive = true,
                duration = 30000L // 30 seconds
            ),
            EmergencyProgressView.EmergencyStep(
                id = 5,
                title = "Continue CPR",
                description = "30 compressions, then 2 breaths, repeat",
                isTimeSensitive = true,
                duration = 120000L // 2 minutes
            )
        )
    }



    private fun checkAndRequestPermissions() {
        // Always check permissions, even if we've requested before
        // This handles cases where user grants permission from settings
        viewModel.checkPermissions()
 
        val missingPermissions = viewModel.getMissingPermissions()
        if (missingPermissions.isEmpty()) {
            // All permissions granted, initialize
            Log.d("VoiceAssistantFragment", "All permissions already granted, initializing")
            viewModel.initialize()
            hasRequestedPermissions = false // Reset flag when permissions are granted
        } else {
            // Only show dialog if we haven't already requested in this session
            if (!hasRequestedPermissions) {
                Log.d("VoiceAssistantFragment", "Missing permissions: $missingPermissions, showing rationale")
                // Show rationale dialog
                showPermissionRationaleDialog(missingPermissions)
                hasRequestedPermissions = true
            } else {
                // User already dismissed the dialog in this session, don't spam them
                Log.d("VoiceAssistantFragment", "Already requested permissions in this session, skipping dialog")
            }
        }
    }

    private fun showPermissionRationaleDialog(permissions: List<String>) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permissions Required")
            .setMessage(
                "The Voice Assistant needs the following permissions to function properly:\n\n" +
                        permissions.joinToString("\n") { permission ->
                            "• " + viewModel.getPermissionRationale(permission)
                        }
            )
            .setPositiveButton("Grant") { _, _ ->
                permissionLauncher.launch(permissions.toTypedArray())
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    "Voice Assistant requires permissions to work",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permissions Denied")
            .setMessage(
                "The Voice Assistant cannot function without the required permissions. " +
                        "You can grant permissions in app settings.\n\n" +
                        "Required:\n" +
                        "• Microphone - for voice commands\n" +
                        "• Location - for finding nearby hospitals\n" +
                        "• Phone - for emergency calls"
            )
            .setPositiveButton("Open Settings") { dialog, _ ->
                dialog.dismiss()
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun openAppSettings() {
        try {
            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = android.net.Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Unable to open settings. Please enable permissions manually.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateCurrentEmergencyStep() {
        val currentStep = binding.emergencyProgress.getCurrentStep()
        currentStep?.let { step ->
            binding.tvCurrentStepNumber.text = step.id.toString()
            binding.tvCurrentStepTitle.text = step.title
            binding.tvCurrentStepInstructions.text = step.description

            // Show/hide timer for time-sensitive steps
            if (step.isTimeSensitive && step.duration > 0) {
                binding.timerContainer.visibility = View.VISIBLE
                startStepTimer(step.duration)
            } else {
                binding.timerContainer.visibility = View.GONE
            }
        }
    }

    private fun completeCurrentEmergencyStep() {
        binding.emergencyProgress.completeCurrentStep()
        updateCurrentEmergencyStep()

        // Provide haptic feedback
        binding.btnStepComplete.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)

        // Check if all steps are completed
        if (binding.emergencyProgress.isCompleted()) {
            showEmergencyCompleteDialog()
        } else {
            // Voice feedback for step completion
            viewModel.provideFeedback("Step completed. Moving to next step.")
        }
    }

    private fun repeatCurrentEmergencyStep() {
        // Voice feedback to repeat current step instructions
        val currentStep = binding.emergencyProgress.getCurrentStep()
        currentStep?.let { step ->
            viewModel.provideFeedback("Repeating: ${step.title}. ${step.description}")
        }

        // Provide haptic feedback
        binding.btnRepeatStep.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
    }

    private var stepTimer: android.os.CountDownTimer? = null

    private fun startStepTimer(durationMs: Long) {
        stepTimer?.cancel()

        stepTimer = object : android.os.CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.tvStepTimer.text = String.format("%02d:%02d", minutes, seconds)

                // Change color based on remaining time
                when {
                    millisUntilFinished < 10000 -> // Less than 10 seconds
                        binding.tvStepTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.emergency_timer_urgent))
                    millisUntilFinished < 30000 -> // Less than 30 seconds
                        binding.tvStepTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.emergency_timer_warning))
                    else ->
                        binding.tvStepTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.emergency_timer_normal))
                }
            }

            override fun onFinish() {
                binding.tvStepTimer.text = "00:00"
                binding.tvStepTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.emergency_timer_urgent))

                // Auto-advance to next step or prompt user
                viewModel.provideFeedback("Time completed. Ready for next step.")
                binding.btnStepComplete.performClick()
            }
        }.start()
    }

    private fun callEmergencyServices() {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_CALL)
            intent.data = android.net.Uri.parse("tel:112")

            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            } else {
                // Fallback to dialer
                val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                dialIntent.data = android.net.Uri.parse("tel:112")
                startActivity(dialIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to place call. Please dial 112 manually.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showEmergencyCompleteDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Training Procedure Complete")
            .setMessage("You have completed all training steps. In real situations, continue monitoring and wait for help to arrive.")
            .setPositiveButton("Continue Monitoring") { _, _ ->
                // Keep mode active but show completion state
                viewModel.provideFeedback("Training procedure completed. Continue monitoring.")
            }
            .setNegativeButton("Exit Training Mode") { _, _ ->
                viewModel.exitEmergencyMode()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Format AI response text by removing markdown and making it more readable
     */
    private fun formatAIResponse(text: String): android.text.SpannableStringBuilder {
        val builder = android.text.SpannableStringBuilder()

        // Split into lines for processing
        val lines = text.split("\n")

        for (line in lines) {
            var processedLine = line

            // Remove markdown symbols but preserve the formatting intent
            when {
                // Headers (##, ###, etc.)
                processedLine.trim().startsWith("##") -> {
                    processedLine = processedLine.replace(Regex("^#+\\s*"), "")
                    val start = builder.length
                    builder.append(processedLine)
                    builder.append("\n\n")
                    // Make it bold and larger
                    builder.setSpan(
                        android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        start, builder.length - 2,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    builder.setSpan(
                        android.text.style.RelativeSizeSpan(1.2f),
                        start, builder.length - 2,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                // Bold text (**text**)
                processedLine.contains("**") -> {
                    processedLine = processedLine.replace("**", "")
                    val start = builder.length
                    builder.append(processedLine)
                    builder.append("\n")
                    builder.setSpan(
                        android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        start, builder.length - 1,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                // Bullet points
                processedLine.trim().startsWith("•") || processedLine.trim().startsWith("-") -> {
                    processedLine = processedLine.replace(Regex("^[•\\-]\\s*"), "  • ")
                    builder.append(processedLine)
                    builder.append("\n")
                }
                // Numbered lists
                processedLine.trim().matches(Regex("^\\d+\\..*")) -> {
                    builder.append("  ")
                    builder.append(processedLine.trim())
                    builder.append("\n")
                }
                // Empty lines
                processedLine.trim().isEmpty() -> {
                    builder.append("\n")
                }
                // Regular text
                else -> {
                    builder.append(processedLine)
                    builder.append("\n")
                }
            }
        }

        return builder
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stepTimer?.cancel()
        // Shutdown voice assistant to prevent memory leaks
        viewModel.shutdown()
        _binding = null
    }
}
