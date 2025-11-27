package com.mediguide.firstaid.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.mediguide.firstaid.databinding.FragmentSettingsBinding
import com.mediguide.firstaid.utils.UserPreferencesManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferencesManager: UserPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = UserPreferencesManager(requireContext())
        setupViews()
        loadPreferences()
        setupListeners()
    }

    private fun setupViews() {
        // Initialize views and set up the UI
        binding.toolbar.title = "Settings"
    }

    private fun loadPreferences() {
        // Load current preferences and update UI
        binding.switchSound.isChecked = preferencesManager.isSoundEnabled
        binding.switchVibration.isChecked = preferencesManager.isVibrationEnabled
        binding.switchEmergencyConfirm.isChecked = preferencesManager.requireEmergencyConfirmation
        binding.switchShowImages.isChecked = preferencesManager.showImages
        binding.switchSaveHistory.isChecked = preferencesManager.saveSearchHistory
        binding.switchQuickDial.isChecked = preferencesManager.quickDialEnabled
    }

    private fun setupListeners() {
        // App settings
        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.isSoundEnabled = isChecked
        }

        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.isVibrationEnabled = isChecked
        }

        binding.switchEmergencyConfirm.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.requireEmergencyConfirmation = isChecked
        }

        // Guide settings
        binding.switchShowImages.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.showImages = isChecked
        }

        // Search settings
        binding.switchSaveHistory.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.saveSearchHistory = isChecked
        }

        // Emergency settings
        binding.switchQuickDial.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.quickDialEnabled = isChecked
        }

        // Reset to defaults button
        binding.btnResetDefaults.setOnClickListener {
            resetToDefaults()
        }
    }

    private fun resetToDefaults() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset Settings")
            .setMessage("Are you sure you want to reset all settings to defaults? This cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                preferencesManager.resetToDefaults()
                loadPreferences()
                Toast.makeText(requireContext(), "Settings reset to defaults", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
