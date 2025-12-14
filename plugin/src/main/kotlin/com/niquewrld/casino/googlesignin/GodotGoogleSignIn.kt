package com.niquewrld.casino.googlesignin

import android.app.Activity
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot
import java.security.MessageDigest
import java.util.UUID

/**
 * Godot Android Plugin for Google Sign-In using Credential Manager API
 */
class GodotGoogleSignIn(godot: Godot) : GodotPlugin(godot) {
    
    companion object {
        private const val TAG = "GodotGoogleSignIn"
    }
    
    private var credentialManager: CredentialManager? = null
    private var webClientId: String = ""
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    // Signals
    private val signInSuccessSignal = SignalInfo("sign_in_success", String::class.java, String::class.java, String::class.java)
    private val signInFailedSignal = SignalInfo("sign_in_failed", String::class.java)
    private val signOutCompleteSignal = SignalInfo("sign_out_complete")
    
    override fun getPluginName(): String = "GodotGoogleSignIn"
    
    override fun getPluginSignals(): Set<SignalInfo> {
        return setOf(signInSuccessSignal, signInFailedSignal, signOutCompleteSignal)
    }
    
    private fun getOrCreateCredentialManager(): CredentialManager? {
        if (credentialManager == null) {
            val act = activity
            if (act != null) {
                credentialManager = CredentialManager.create(act)
                Log.d(TAG, "CredentialManager created")
            }
        }
        return credentialManager
    }
    
    /**
     * Initialize the plugin with your Web Client ID from Google Cloud Console
     */
    @UsedByGodot
    fun initialize(webClientId: String) {
        this.webClientId = webClientId
        Log.d(TAG, "Initialized with Web Client ID")
    }
    
    /**
     * Check if the plugin is properly initialized
     */
    @UsedByGodot
    fun isInitialized(): Boolean {
        return webClientId.isNotEmpty()
    }
    
    /**
     * Start Google Sign-In flow
     * This will show the Google Sign-In bottom sheet
     */
    @UsedByGodot
    fun signIn() {
        if (webClientId.isEmpty()) {
            emitSignal(signInFailedSignal.name, "Plugin not initialized. Call initialize() first.")
            return
        }
        
        val act = activity ?: run {
            emitSignal(signInFailedSignal.name, "Activity not available")
            return
        }
        
        val cm = getOrCreateCredentialManager() ?: run {
            emitSignal(signInFailedSignal.name, "CredentialManager not available")
            return
        }
        
        coroutineScope.launch {
            try {
                // Generate a nonce for security
                val nonce = generateNonce()
                
                // Try to sign in with existing authorized account first
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(true)
                    .setNonce(nonce)
                    .build()
                
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                
                try {
                    val result = cm.getCredential(act, request)
                    handleSignInResult(result)
                } catch (e: GetCredentialException) {
                    // No authorized accounts found, try with all accounts
                    Log.d(TAG, "No authorized accounts, trying with all accounts")
                    signInWithAllAccounts(act, nonce, cm)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign-in failed", e)
                emitSignal(signInFailedSignal.name, e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Sign in allowing user to choose from all Google accounts
     */
    @UsedByGodot
    fun signInWithAccountChooser() {
        if (webClientId.isEmpty()) {
            emitSignal(signInFailedSignal.name, "Plugin not initialized. Call initialize() first.")
            return
        }
        
        val act = activity ?: run {
            emitSignal(signInFailedSignal.name, "Activity not available")
            return
        }
        
        val cm = getOrCreateCredentialManager() ?: run {
            emitSignal(signInFailedSignal.name, "CredentialManager not available")
            return
        }
        
        coroutineScope.launch {
            try {
                val nonce = generateNonce()
                signInWithAllAccounts(act, nonce, cm)
            } catch (e: Exception) {
                Log.e(TAG, "Sign-in failed", e)
                emitSignal(signInFailedSignal.name, e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Use the Sign in with Google button flow
     */
    @UsedByGodot
    fun signInWithGoogleButton() {
        if (webClientId.isEmpty()) {
            emitSignal(signInFailedSignal.name, "Plugin not initialized. Call initialize() first.")
            return
        }
        
        val act = activity ?: run {
            emitSignal(signInFailedSignal.name, "Activity not available")
            return
        }
        
        val cm = getOrCreateCredentialManager() ?: run {
            emitSignal(signInFailedSignal.name, "CredentialManager not available")
            return
        }
        
        coroutineScope.launch {
            try {
                val nonce = generateNonce()
                
                val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
                    .setNonce(nonce)
                    .build()
                
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()
                
                val result = cm.getCredential(act, request)
                handleSignInResult(result)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Sign-in with Google button failed", e)
                emitSignal(signInFailedSignal.name, e.message ?: "Sign-in cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Sign-in failed", e)
                emitSignal(signInFailedSignal.name, e.message ?: "Unknown error")
            }
        }
    }
    
    private suspend fun signInWithAllAccounts(activity: Activity, nonce: String, cm: CredentialManager) {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setNonce(nonce)
            .build()
        
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        
        try {
            val result = cm.getCredential(activity, request)
            handleSignInResult(result)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Sign-in with all accounts failed", e)
            emitSignal(signInFailedSignal.name, e.message ?: "Sign-in cancelled")
        }
    }
    
    private fun handleSignInResult(result: GetCredentialResponse) {
        val credential = result.credential
        
        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        
                        val idToken = googleIdTokenCredential.idToken
                        val email = googleIdTokenCredential.id
                        val displayName = googleIdTokenCredential.displayName ?: ""
                        
                        Log.d(TAG, "Sign-in successful: $email")
                        
                        // Emit success signal with ID token, email, and display name
                        emitSignal(signInSuccessSignal.name, idToken, email, displayName)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Invalid Google ID token", e)
                        emitSignal(signInFailedSignal.name, "Invalid Google ID token")
                    }
                } else {
                    Log.e(TAG, "Unexpected credential type: ${credential.type}")
                    emitSignal(signInFailedSignal.name, "Unexpected credential type")
                }
            }
            else -> {
                Log.e(TAG, "Unexpected credential class: ${credential::class.java.name}")
                emitSignal(signInFailedSignal.name, "Unexpected credential type")
            }
        }
    }
    
    /**
     * Sign out and clear credential state
     */
    @UsedByGodot
    fun signOut() {
        val cm = getOrCreateCredentialManager()
        coroutineScope.launch {
            try {
                cm?.clearCredentialState(ClearCredentialStateRequest())
                Log.d(TAG, "Sign-out complete")
                emitSignal(signOutCompleteSignal.name)
            } catch (e: Exception) {
                Log.e(TAG, "Sign-out failed", e)
                // Still emit complete since local state should be cleared
                emitSignal(signOutCompleteSignal.name)
            }
        }
    }
    
    /**
     * Generate a secure nonce for the sign-in request
     */
    private fun generateNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
