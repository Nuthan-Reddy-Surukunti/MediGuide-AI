package com.mediguide.firstaid.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mediguide.firstaid.R
import com.mediguide.firstaid.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    // ViewBinding instance for the fragment's views
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    // Static list of training tips shown in the UI
    private val emergencyTips = listOf(
        "For educational purposes only. Call 112 in real emergencies",
        "Stay calm and assess the situation before taking action",
        "Never move an injured person unless they're in immediate danger",
        "Check for breathing and pulse before starting CPR",
        "Apply direct pressure to stop severe bleeding",
        "Keep contact numbers easily accessible"
    )

    // ActivityResult launcher for requesting a single permission (CALL_PHONE)
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            // If permission granted, attempt the emergency call, otherwise open dialer
            if (isGranted) {
                makeEmergencyCall()
            } else {
                Toast.makeText(requireContext(), "Permission denied. Opening dialer instead.", Toast.LENGTH_SHORT).show()
                makeEmergencyDialerCall()
            }
        }

    // Inflate the fragment layout using view binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Set up UI when view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Wire up click listeners and initial UI state
        setupClickListeners()
        setupEmergencyTip()
    }

    // Attach click listeners for buttons and cards on the home screen
    private fun setupClickListeners() {

        // Quick Action Cards - small shortcuts to specific guides
        binding.cardCPR.setOnClickListener {
            navigateToGuide("CPR")
        }

        binding.cardFractures.setOnClickListener {
            navigateToGuide("Fractures")
        }

        binding.cardBurns.setOnClickListener {
            navigateToGuide("Burns")
        }

        binding.cardBleeding.setOnClickListener {
            navigateToGuide("Severe Bleeding")
        }

        // Emergency Scenario Cards - navigate to relevant guides
        binding.cardHeartAttack.setOnClickListener {
            navigateToGuide("Heart Attack")
        }

        binding.cardStroke.setOnClickListener {
            navigateToGuide("Stroke")
        }

        binding.cardAllergicReaction.setOnClickListener {
            navigateToGuide("Allergic Reactions")
        }

        // AI Assistant Quick Access - navigate to voice assistant screen
        binding.cardAIAssistant.setOnClickListener {
            try {
                findNavController().navigate(R.id.navigation_voice_assistant)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Failed to navigate to AI Assistant", e)
                Toast.makeText(requireContext(), "Unable to open AI Assistant", Toast.LENGTH_SHORT).show()
            }
        }

        // Emergency Tip Card - show a different tip when tapped
        binding.cardEmergencyTip.setOnClickListener {
            showRandomTip()
        }
    }

    // Prepare and show the initial emergency tip
    private fun setupEmergencyTip() {
        // Display one initial random tip
        showRandomTip()
    }

    // Choose a random tip from the list and display it
    private fun showRandomTip() {
        val randomTip = emergencyTips.random()
        binding.tvEmergencyTip.text = randomTip
    }

    // Search for a guide by title and navigate to its detail
    private fun navigateToGuide(guideTitle: String) {
        lifecycleScope.launch {
            try {
                // Get ViewModel (must be created on main thread)
                val viewModel: HomeViewModel by activityViewModels()

                // Search for the guide (fast operation, no background thread needed)
                val searchResults = viewModel.searchGuidesByTitle(guideTitle)

                if (searchResults.isNotEmpty()) {
                    // Prefer exact match, otherwise take first result
                    val guide = searchResults.firstOrNull {
                        it.title.equals(guideTitle, ignoreCase = true)
                    } ?: searchResults.first()

                    // Navigate to GuideDetailFragment with the guide ID
                    val action = HomeFragmentDirections.actionHomeToGuideDetail(guide.id)
                    findNavController().navigate(action)
                    Log.d("HomeFragment", "Navigating to guide: ${guide.title} with ID: ${guide.id}")
                } else {
                    // No match -> open the All Guides screen
                    Log.w("HomeFragment", "Guide not found: $guideTitle, navigating to All Guides")
                    findNavController().navigate(R.id.navigation_all_guides)
                    Toast.makeText(requireContext(), "Guide not found. Showing all guides...", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle unexpected errors during navigation/search
                Log.e("HomeFragment", "Failed to navigate to guide: $guideTitle", e)
                Toast.makeText(requireContext(), "Unable to open guide", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle the emergency call flow including permissions and fallbacks
    private fun makeEmergencyCall() {
        // Guard against view not available or fragment not attached
        if (_binding == null || !isAdded) {
            Log.w("HomeFragment", "Cannot make call - fragment not ready")
            return
        }

        try {
            // Check CALL_PHONE permission and act accordingly
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission granted -> attempt direct CALL intent
                    Log.d("HomeFragment", "CALL_PHONE permission granted, making direct call")
                    Toast.makeText(requireContext(), "Calling Emergency Services (112)...", Toast.LENGTH_SHORT).show()

                    try {
                        val callIntent = Intent(Intent.ACTION_CALL).apply {
                            data = "tel:112".toUri()
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }

                        // Check if any app can handle the call
                        val packageManager = requireContext().packageManager
                        if (callIntent.resolveActivity(packageManager) != null) {
                            Log.d("HomeFragment", "Starting emergency call activity")
                            startActivity(callIntent)
                            Log.d("HomeFragment", "Call activity started successfully")
                        } else {
                            // Fallback to dialer if CALL cannot be handled
                            Log.e("HomeFragment", "No app to handle call intent, falling back to dialer")
                            makeEmergencyDialerCall()
                        }
                    } catch (securityException: SecurityException) {
                        Log.e("HomeFragment", "SecurityException during call", securityException)
                        Toast.makeText(requireContext(), "Call permission denied. Opening dialer.", Toast.LENGTH_SHORT).show()
                        makeEmergencyDialerCall()
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Exception during call", e)
                        Toast.makeText(requireContext(), "Error initiating call. Opening dialer.", Toast.LENGTH_SHORT).show()
                        makeEmergencyDialerCall()
                    }
                }
                // Show rationale and request permission if needed
                shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE) -> {
                    Log.d("HomeFragment", "Showing permission rationale")
                    Toast.makeText(requireContext(), "Call permission is needed to make emergency calls.", Toast.LENGTH_LONG).show()
                    requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                }
                else -> {
                    // Directly request permission when no rationale required
                    Log.d("HomeFragment", "Requesting CALL_PHONE permission")
                    requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                }
            }
        } catch (e: Exception) {
            // If anything fails, fallback to opening the dialer
            Log.e("HomeFragment", "Failed to initiate emergency call", e)
            Toast.makeText(requireContext(), "Direct call failed. Opening dialer.", Toast.LENGTH_SHORT).show()
            makeEmergencyDialerCall()
        }
    }

    // Open the dialer with the emergency number as fallback
    private fun makeEmergencyDialerCall() {
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = "tel:112".toUri()
            }
            startActivity(dialIntent)
            Log.d("HomeFragment", "Emergency dialer opened successfully")
        } catch (e: Exception) {
            Log.e("HomeFragment", "Failed to open emergency dialer", e)
            Toast.makeText(requireContext(), "Unable to initiate call", Toast.LENGTH_SHORT).show()
        }
    }

    // Clear binding when view is destroyed to avoid leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
