package com.mediguide.firstaid.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mediguide.firstaid.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class SignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private lateinit var emailInput: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordInput: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmInput: TextInputEditText
    private lateinit var confirmLayout: TextInputLayout
    private lateinit var btnSignup: MaterialButton
    private lateinit var btnGoToLogin: MaterialButton
    private lateinit var progress: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        emailInput = view.findViewById(R.id.et_email)
        emailLayout = view.findViewById(R.id.layout_email)
        passwordInput = view.findViewById(R.id.et_password)
        passwordLayout = view.findViewById(R.id.layout_password)
        confirmInput = view.findViewById(R.id.et_confirm_password)
        confirmLayout = view.findViewById(R.id.layout_confirm_password)
        btnSignup = view.findViewById(R.id.btn_signup)
        btnGoToLogin = view.findViewById(R.id.btn_go_to_login)
        progress = view.findViewById(R.id.progress)

        btnSignup.setOnClickListener { signupWithEmail() }
        btnGoToLogin.setOnClickListener { findNavController().navigate(R.id.action_signup_to_login) }
    }

    private fun signupWithEmail() {
        val email = emailInput.text?.toString()?.trim().orEmpty()
        val password = passwordInput.text?.toString()?.trim().orEmpty()
        val confirm = confirmInput.text?.toString()?.trim().orEmpty()

        var valid = true
        if (email.isEmpty()) { emailLayout.error = "Email required"; valid = false } else { emailLayout.error = null }
        if (password.length < 6) { passwordLayout.error = "Min 6 characters"; valid = false } else { passwordLayout.error = null }
        if (confirm != password) { confirmLayout.error = "Passwords do not match"; valid = false } else { confirmLayout.error = null }
        if (!valid) return

        setLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    // Save user info from Firebase Auth
                    saveUserInfoFromFirebase()
                    Toast.makeText(requireContext(), "Account created. You are logged in.", Toast.LENGTH_LONG).show()
                    navigateToHome()
                } else {
                    Toast.makeText(requireContext(), task.exception?.localizedMessage ?: "Sign up failed", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserInfoFromFirebase() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val prefsManager = com.mediguide.firstaid.utils.UserPreferencesManager(requireContext())

            // Save display name if available and not already set
            currentUser.displayName?.let { displayName ->
                if (displayName.isNotEmpty() && prefsManager.userName.isEmpty()) {
                    prefsManager.userName = displayName
                }
            }
            
            // Save photo URL if available and not already set
            currentUser.photoUrl?.let { photoUrl ->
                if (prefsManager.profileImageUri.isEmpty()) {
                    prefsManager.profileImageUri = photoUrl.toString()
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        btnSignup.isEnabled = !loading
        btnGoToLogin.isEnabled = !loading
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.navigation_home)
    }
}

