package com.howsmymind.core.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricAuth(
    private val activity: FragmentActivity,
    private val onSuccess: () -> Unit,
    private val onFailure: () -> Unit,
    private val onError: (String) -> Unit
) {
    private val executor = ContextCompat.getMainExecutor(activity)

    private val callBack = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onSuccess()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onFailure()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onError(errString.toString())
        }
    }

    private val prompt = BiometricPrompt(activity, executor, callBack)

    private val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock")
        .setSubtitle("Verify with biometrics")
        .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
        .build()

    fun show() {
        if (isBiometricAvailable(activity)) {
            prompt.authenticate(info)
        } else {
            onError("Biometric auth is not available on this device")
        }
    }

    companion object {
        fun isBiometricAvailable(context: Context): Boolean {
            val bm = BiometricManager.from(context)
            val result = bm.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)

            return result == BiometricManager.BIOMETRIC_SUCCESS
        }
    }
}