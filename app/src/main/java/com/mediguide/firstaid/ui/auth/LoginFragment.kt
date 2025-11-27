package com.mediguide.firstaid.ui.auth

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mediguide.firstaid.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private lateinit var emailInput: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordInput: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoogle: MaterialButton
    private lateinit var btnGoToSignup: MaterialButton
    private lateinit var progress: ProgressBar

    private val googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("LoginFragment", "Google Sign-In result: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("LoginFragment", "Google account obtained: ${account.email}")
                firebaseAuthWithGoogle(account.idToken)
            } catch (e: ApiException) {
                Log.e("LoginFragment", "Google Sign-In failed", e)
                setLoading(false)
                Toast.makeText(requireContext(), "Google sign-in failed: Error ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.w("LoginFragment", "Google Sign-In cancelled")
            setLoading(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        emailInput = view.findViewById(R.id.et_email)
        emailLayout = view.findViewById(R.id.layout_email)
        passwordInput = view.findViewById(R.id.et_password)
        passwordLayout = view.findViewById(R.id.layout_password)
        btnLogin = view.findViewById(R.id.btn_login)
        btnGoogle = view.findViewById(R.id.btn_google)
        btnGoToSignup = view.findViewById(R.id.btn_go_to_signup)
        progress = view.findViewById(R.id.progress)

        btnLogin.setOnClickListener { loginWithEmail() }
        btnGoogle.setOnClickListener { signInWithGoogle() }
        btnGoToSignup.setOnClickListener { findNavController().navigate(R.id.action_login_to_signup) }
    }

    private fun loginWithEmail() {
        val email = emailInput.text?.toString()?.trim().orEmpty()
        val password = passwordInput.text?.toString()?.trim().orEmpty()

        var valid = true
        if (email.isEmpty()) { emailLayout.error = "Email required"; valid = false } else { emailLayout.error = null }
        if (password.length < 6) { passwordLayout.error = "Min 6 characters"; valid = false } else { passwordLayout.error = null }
        if (!valid) return

        setLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    // Save user info from Firebase Auth
                    saveUserInfoFromFirebase()
                    navigateToHome()
                } else {
                    Toast.makeText(requireContext(), task.exception?.localizedMessage ?: "Login failed", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun signInWithGoogle() {
        Log.d("LoginFragment", "Starting Google Sign-In")
        setLoading(true)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(requireContext(), gso)
        googleLauncher.launch(client.signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        Log.d("LoginFragment", "Authenticating with Firebase, idToken present: ${idToken != null}")
        if (idToken == null) {
            setLoading(false)
            Toast.makeText(requireContext(), "Google sign-in failed - no ID token", Toast.LENGTH_SHORT).show()
            return
        }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    Log.d("LoginFragment", "Firebase auth successful")
                    // Save user info from Firebase Auth
                    saveUserInfoFromFirebase()
                    Toast.makeText(requireContext(), "Welcome!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } else {
                    Log.e("LoginFragment", "Firebase auth failed", task.exception)
                    Toast.makeText(requireContext(), "Sign-in failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
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
        btnLogin.isEnabled = !loading
        btnGoogle.isEnabled = !loading
        btnGoToSignup.isEnabled = !loading
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.navigation_home)
    }
}

