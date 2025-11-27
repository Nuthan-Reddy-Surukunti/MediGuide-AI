package com.mediguide.firstaid.ui.guide

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mediguide.firstaid.R
import com.mediguide.firstaid.databinding.FragmentGuideDetailBinding
import com.mediguide.firstaid.data.models.FirstAidGuide

import com.mediguide.firstaid.utils.Constants
import com.mediguide.firstaid.utils.UserPreferencesManager
import com.mediguide.firstaid.utils.YouTubeVideoHelper
import java.io.IOException

class GuideDetailFragment : Fragment() {

    private var _binding: FragmentGuideDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: GuideDetailViewModel
    private lateinit var stepsAdapter: GuideStepsAdapter
    private var isDetailedInstructionsVisible = false
    private lateinit var prefsManager: UserPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuideDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize preferences manager for tracking learning progress
        prefsManager = UserPreferencesManager(requireContext())

        setupViewModel()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        // Get guide ID from arguments
        val guideId = arguments?.getString("guide_id") ?: ""
        if (guideId.isNotEmpty()) {
            viewModel.loadGuide(guideId)

            // Track that user opened this guide for learning progress
            trackGuideOpened(guideId)
        }
    }

    private fun trackGuideOpened(guideId: String) {
        // Convert guide ID to a numeric ID for tracking
        val numericId = guideId.hashCode().let { if (it < 0) -it else it } % 100
        prefsManager.markGuideAsOpened(numericId)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[GuideDetailViewModel::class.java]
    }

    private fun setupRecyclerView() {
        stepsAdapter = GuideStepsAdapter(
            onStepCompleted = { step ->
                viewModel.markStepCompleted(step)
            },
            onVideoPlay = { videoUrl ->
                playVideo(videoUrl)
            },
            onStepExpanded = { step ->
                // Handle step expansion analytics
            }
        )

        binding.rvSteps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = stepsAdapter
        }
    }

    private fun setupObservers() {
        viewModel.guide.observe(viewLifecycleOwner) { guide ->
            guide?.let {
                binding.tvGuideTitle.text = it.title
                binding.tvCategory.text = it.category

                // Handle warnings - check for null or empty list
                binding.tvWarnings.text = if (!it.warnings.isNullOrEmpty()) {
                    it.warnings.joinToString("\n• ", "• ")
                } else {
                    "No specific warnings"
                }

                binding.tvWhenToCall.text = it.whenToCallEmergency

                // Prepare detailed instructions content but keep hidden by default
                binding.tvDetailedDescription.text = createDetailedInstructions(it)
                binding.cvDetailedInstructions.visibility = View.GONE
                isDetailedInstructionsVisible = false

                // Create new adapter with guide name for better image context
                stepsAdapter = GuideStepsAdapter(
                    onStepCompleted = { step ->
                        viewModel.markStepCompleted(step)
                    },
                    onVideoPlay = { videoUrl ->
                        playVideo(videoUrl)
                    },
                    onStepExpanded = { step ->
                        // Analytics tracking for step expansion
                    },
                    guideName = it.title
                )

                binding.rvSteps.adapter = stepsAdapter
                stepsAdapter.submitList(it.steps)

                // Load demonstration photo for this guide
                loadGuidePhoto(it.title)

                // Setup video tutorial button
                setupVideoButton(it)
            }
        }
    }

    private fun setupVideoButton(guide: FirstAidGuide) {
        // Get video link from helper or from guide data
        val videoUrl = guide.youtubeLink ?: YouTubeVideoHelper.getVideoLinkForGuide(guide.id)

        if (videoUrl != null) {
            // Video is available - show button
            binding.btnWatchVideo?.visibility = View.VISIBLE
            binding.btnWatchVideo?.text = "🎥 ${YouTubeVideoHelper.getVideoTitle(guide.id)}"
            binding.btnWatchVideo?.setOnClickListener {
                YouTubeVideoHelper.openVideo(requireContext(), videoUrl)
            }
        } else {
            // No video available - hide button
            binding.btnWatchVideo?.visibility = View.GONE
        }
    }

    private fun createDetailedInstructions(guide: FirstAidGuide): String {
        val sb = StringBuilder()

        sb.append(guide.description)
        sb.append("\n\n")

        sb.append("📋 Preparation:\n")
        sb.append("• Ensure the scene is safe before approaching\n")
        sb.append("• Check for responsiveness and breathing\n")
        sb.append("• Call for help if needed\n")
        sb.append("• Gather any required materials\n\n")

        when {
            guide.title.contains("CPR", ignoreCase = true) -> {
                sb.append("🫀 CPR Guidelines:\n")
                sb.append("• Push hard and fast at least 2 inches deep\n")
                sb.append("• Allow complete chest recoil between compressions\n")
                sb.append("• Minimize interruptions\n")
                sb.append("• Switch providers every 2 minutes to avoid fatigue\n\n")
            }
            guide.title.contains("Choking", ignoreCase = true) -> {
                sb.append("🫁 Choking Response:\n")
                sb.append("• Encourage coughing if the person can still breathe\n")
                sb.append("• Be prepared to catch the person if they become unconscious\n")
                sb.append("• Continue until object is expelled or person becomes unconscious\n\n")
            }
            guide.title.contains("Burns", ignoreCase = true) -> {
                sb.append("🔥 Burn Care Principles:\n")
                sb.append("• Cool the burn immediately with cool running water\n")
                sb.append("• Remove from heat source safely\n")
                sb.append("• Do not use ice, butter, or other home remedies\n")
                sb.append("• Protect the area from further damage\n\n")
            }
        }

        if (guide.estimatedTimeMinutes > 0) {
            sb.append("⏱️ Estimated Time: ${guide.estimatedTimeMinutes} minutes\n")
        }

        sb.append("📊 Difficulty Level: ${guide.difficulty}\n\n")

        sb.append("🚨 Seek Professional Help When:\n")
        guide.warnings?.forEach { warning ->
            sb.append("• $warning\n")
        }

        return sb.toString()
    }

    private fun loadGuidePhoto(guideTitle: String) {
        // Check if images should be shown based on user preference
        if (prefsManager.showImages) {
            binding.cvPhotoDemo.visibility = View.VISIBLE
            // Photo loading logic would go here
        } else {
            binding.cvPhotoDemo.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.btnCallEmergency.setOnClickListener {
            makeEmergencyCall()
        }

        binding.tvViewFullPhoto.setOnClickListener {
            showFullSizePhoto()
        }

        binding.ivGuidePhoto.setOnClickListener {
            showFullSizePhoto()
        }

        binding.btnToggleDetails.setOnClickListener {
            toggleDetailedInstructions()
        }
    }

    private fun toggleDetailedInstructions() {
        isDetailedInstructionsVisible = !isDetailedInstructionsVisible

        if (isDetailedInstructionsVisible) {
            binding.cvDetailedInstructions.visibility = View.VISIBLE
            binding.btnToggleDetails.text = "Hide Detailed Instructions"
            binding.btnToggleDetails.setIconResource(R.drawable.ic_expand_less)
        } else {
            binding.cvDetailedInstructions.visibility = View.GONE
            binding.btnToggleDetails.text = "Show Detailed Instructions"
            binding.btnToggleDetails.setIconResource(R.drawable.ic_expand_more)
        }
    }

    private fun showFullSizePhoto() {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val imageView = ImageView(requireContext())
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE

        binding.ivGuidePhoto.drawable?.let { drawable ->
            imageView.setImageDrawable(drawable)
        }

        imageView.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(imageView)
        dialog.show()
    }

    private fun makeEmergencyCall() {
        try {
            // Check if emergency confirmation is required
            if (prefsManager.requireEmergencyConfirmation) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Quick Call")
                    .setMessage("Are you sure you want to call ${Constants.EMERGENCY_NUMBER_IN}?")
                    .setPositiveButton("Call") { _, _ ->
                        performEmergencyCall()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                performEmergencyCall()
            }
        } catch (e: Exception) {
            android.util.Log.e("GuideDetailFragment", "Error: ${e.message}")
            performEmergencyCall() // Fallback
        }
    }

    private fun performEmergencyCall() {
        try {
            // Use ACTION_DIAL to open the dialer with the number pre-filled
            // This doesn't require CALL_PHONE permission and gives user control
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = "tel:${Constants.EMERGENCY_NUMBER_IN}".toUri()
            }
            startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("GuideDetailFragment", "Error opening dialer: ${e.message}")
            // Show a toast if unable to open dialer
            android.widget.Toast.makeText(
                requireContext(),
                "Unable to open phone dialer",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun playVideo(videoUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(videoUrl.toUri(), "video/*")
            }

            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                val browserIntent = Intent(Intent.ACTION_VIEW, videoUrl.toUri())
                startActivity(browserIntent)
            }
        } catch (e: Exception) {
            android.util.Log.e("GuideDetailFragment", "Error playing video: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

