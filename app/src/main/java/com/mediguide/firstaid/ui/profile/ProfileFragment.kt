package com.mediguide.firstaid.ui.profile

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.mediguide.firstaid.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth

/**
 * ProfileFragment - User profile and medical information
 */
class ProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel

    // Profile UI elements
    private lateinit var tvProfileInitials: TextView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileBio: TextView
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnSignOut: MaterialButton
    private lateinit var btnResetDefaults: MaterialButton

    // Settings switches
    private lateinit var switchSound: SwitchMaterial
    private lateinit var switchVibration: SwitchMaterial
    private lateinit var switchEmergencyConfirm: SwitchMaterial
    private lateinit var switchShowImages: SwitchMaterial
    private lateinit var switchSaveHistory: SwitchMaterial
    private lateinit var switchQuickDial: SwitchMaterial

    // Medical Info UI elements
    private lateinit var cardMedicalInfo: CardView
    private lateinit var tvBloodType: TextView
    private lateinit var tvAllergies: TextView
    private lateinit var tvMedications: TextView
    private lateinit var tvMedicalConditions: TextView
    private lateinit var tvEmergencyNotes: TextView
    private lateinit var tvDoctorInfo: TextView
    private lateinit var btnEditMedicalInfo: MaterialButton

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Save the image URI and update profile
            viewModel.saveProfileImage(it.toString())
            loadProfileImage(it.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // Initialize UI elements
        initViews(view)

        // Observe data
        observeData()

        // Set up listeners
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        // Refresh profile data from Firebase Auth when fragment is displayed
        viewModel.syncFromFirebaseAuth()
    }

    private fun initViews(view: View) {
        // Profile elements
        tvProfileInitials = view.findViewById(R.id.tv_profile_initials)
        tvProfileName = view.findViewById(R.id.tv_profile_name)
        tvProfileBio = view.findViewById(R.id.tv_profile_bio)
        btnEditProfile = view.findViewById(R.id.btn_edit_profile)
        btnSignOut = view.findViewById(R.id.btn_sign_out)
        btnResetDefaults = view.findViewById(R.id.btn_reset_defaults)

        // Settings switches
        switchSound = view.findViewById(R.id.switch_sound)
        switchVibration = view.findViewById(R.id.switch_vibration)
        switchEmergencyConfirm = view.findViewById(R.id.switch_emergency_confirm)
        switchShowImages = view.findViewById(R.id.switch_show_images)
        switchSaveHistory = view.findViewById(R.id.switch_save_history)
        switchQuickDial = view.findViewById(R.id.switch_quick_dial)

        // Medical info elements
        cardMedicalInfo = view.findViewById(R.id.card_medical_info)
        tvBloodType = view.findViewById(R.id.tv_blood_type)
        tvAllergies = view.findViewById(R.id.tv_allergies)
        tvMedications = view.findViewById(R.id.tv_medications)
        tvMedicalConditions = view.findViewById(R.id.tv_medical_conditions)
        tvEmergencyNotes = view.findViewById(R.id.tv_emergency_notes)
        tvDoctorInfo = view.findViewById(R.id.tv_doctor_info)
        btnEditMedicalInfo = view.findViewById(R.id.btn_edit_medical_info)

        // Load preferences and set switch states
        loadPreferences()

        // Add click listener to profile picture for changing image
        tvProfileInitials.setOnClickListener {
            showImagePickerDialog()
        }
    }

    private fun loadPreferences() {
        val prefsManager = com.mediguide.firstaid.utils.UserPreferencesManager(requireContext())
        switchSound.isChecked = prefsManager.isSoundEnabled
        switchVibration.isChecked = prefsManager.isVibrationEnabled
        switchEmergencyConfirm.isChecked = prefsManager.requireEmergencyConfirmation
        switchShowImages.isChecked = prefsManager.showImages
        switchSaveHistory.isChecked = prefsManager.saveSearchHistory
        switchQuickDial.isChecked = prefsManager.quickDialEnabled
    }

    private fun observeData() {
        // Observe user profile
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            tvProfileInitials.text = viewModel.getUserInitials()
            tvProfileName.text = if (profile.name.isEmpty()) "Set your name" else profile.name
            tvProfileBio.text = if (profile.bio.isEmpty()) "Add a bio" else profile.bio

            // Load profile image if available
            if (profile.profileImageUri.isNotEmpty()) {
                loadProfileImage(profile.profileImageUri)
            }
        }

        // Observe medical info
        viewModel.medicalInfo.observe(viewLifecycleOwner) { medical ->
            tvBloodType.text = if (medical.bloodType.isEmpty()) "Not set" else medical.bloodType
            tvAllergies.text = if (medical.allergies.isEmpty()) "None listed" else medical.allergies
            tvMedications.text = if (medical.medications.isEmpty()) "None listed" else medical.medications
            tvMedicalConditions.text = if (medical.medicalConditions.isEmpty()) "None listed" else medical.medicalConditions
            tvEmergencyNotes.text = if (medical.emergencyNotes.isEmpty()) "None" else medical.emergencyNotes

            val doctorInfo = if (medical.doctorName.isEmpty()) {
                "No doctor information"
            } else {
                "${medical.doctorName}${if (medical.doctorContact.isNotEmpty()) " - ${medical.doctorContact}" else ""}"
            }
            tvDoctorInfo.text = doctorInfo
        }
    }

    private fun setupListeners() {
        val prefsManager = com.mediguide.firstaid.utils.UserPreferencesManager(requireContext())

        // Edit profile button
        btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }


        // Sound effects switch
        switchSound.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isSoundEnabled = isChecked
        }

        // Vibration switch
        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isVibrationEnabled = isChecked
        }

        // Emergency confirmation switch
        switchEmergencyConfirm.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.requireEmergencyConfirmation = isChecked
        }

        // Show images switch
        switchShowImages.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.showImages = isChecked
        }

        // Save history switch
        switchSaveHistory.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.saveSearchHistory = isChecked
        }

        // Quick dial switch
        switchQuickDial.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.quickDialEnabled = isChecked
        }

        // Edit medical info button
        btnEditMedicalInfo.setOnClickListener {
            showEditMedicalInfoDialog()
        }

        // Reset to defaults button
        btnResetDefaults.setOnClickListener {
            resetToDefaults()
        }

        // Sign out with confirmation
        btnSignOut.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(requireContext(), "Signed out", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.loginFragment)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_name)
        val etBio = dialogView.findViewById<EditText>(R.id.et_bio)

        // Pre-fill with current values
        val currentProfile = viewModel.userProfile.value
        etName.setText(currentProfile?.name ?: "")
        etBio.setText(currentProfile?.bio ?: "")

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val bio = etBio.text.toString().trim()
                viewModel.saveUserProfile(name, bio)
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditMedicalInfoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_medical_info, null)

        val etBloodType = dialogView.findViewById<EditText>(R.id.et_blood_type)
        val etAllergies = dialogView.findViewById<EditText>(R.id.et_allergies)
        val etMedications = dialogView.findViewById<EditText>(R.id.et_medications)
        val etMedicalConditions = dialogView.findViewById<EditText>(R.id.et_medical_conditions)
        val etEmergencyNotes = dialogView.findViewById<EditText>(R.id.et_emergency_notes)
        val etDoctorName = dialogView.findViewById<EditText>(R.id.et_doctor_name)
        val etDoctorContact = dialogView.findViewById<EditText>(R.id.et_doctor_contact)

        // Pre-fill with current values
        val currentMedical = viewModel.medicalInfo.value
        etBloodType.setText(currentMedical?.bloodType ?: "")
        etAllergies.setText(currentMedical?.allergies ?: "")
        etMedications.setText(currentMedical?.medications ?: "")
        etMedicalConditions.setText(currentMedical?.medicalConditions ?: "")
        etEmergencyNotes.setText(currentMedical?.emergencyNotes ?: "")
        etDoctorName.setText(currentMedical?.doctorName ?: "")
        etDoctorContact.setText(currentMedical?.doctorContact ?: "")

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Medical Information")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                viewModel.saveMedicalInfo(
                    bloodType = etBloodType.text.toString().trim(),
                    allergies = etAllergies.text.toString().trim(),
                    medications = etMedications.text.toString().trim(),
                    medicalConditions = etMedicalConditions.text.toString().trim(),
                    emergencyNotes = etEmergencyNotes.text.toString().trim(),
                    doctorName = etDoctorName.text.toString().trim(),
                    doctorContact = etDoctorContact.text.toString().trim()
                )
                Toast.makeText(requireContext(), "Medical information updated", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Choose from Gallery", "Remove Photo")
        AlertDialog.Builder(requireContext())
            .setTitle("Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> imagePickerLauncher.launch("image/*")
                    1 -> {
                        viewModel.saveProfileImage("")
                        tvProfileInitials.text = viewModel.getUserInitials()
                        // Reset to showing initials
                        tvProfileInitials.background = requireContext().getDrawable(R.drawable.circle_background)
                    }
                }
            }
            .show()
    }

    private fun loadProfileImage(imageUri: String) {
        try {
            Glide.with(this)
                .load(Uri.parse(imageUri))
                .circleCrop()
                .into(object : com.bumptech.glide.request.target.CustomViewTarget<TextView, android.graphics.drawable.Drawable>(tvProfileInitials) {
                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable,
                        transition: com.bumptech.glide.request.transition.Transition<in android.graphics.drawable.Drawable>?
                    ) {
                        tvProfileInitials.background = resource
                        tvProfileInitials.text = "" // Clear text when showing image
                    }

                    override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                        // Show initials if image fails to load
                        tvProfileInitials.text = viewModel.getUserInitials()
                    }

                    override fun onResourceCleared(placeholder: android.graphics.drawable.Drawable?) {
                        tvProfileInitials.background = placeholder
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            // If loading fails, just show initials
            tvProfileInitials.text = viewModel.getUserInitials()
        }
    }

    private fun resetToDefaults() {
        val prefsManager = com.mediguide.firstaid.utils.UserPreferencesManager(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("Reset Settings")
            .setMessage("Are you sure you want to reset all settings to defaults? This cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                prefsManager.resetToDefaults()
                loadPreferences()
                Toast.makeText(requireContext(), "Settings reset to defaults", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
