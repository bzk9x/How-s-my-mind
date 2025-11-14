package com.howsmymind

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.biometric.BiometricManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val enableBiometricsUnlockPrompt = Intent(this, EnableBiometricsUnlockPromptActivity::class.java)
        val home = Intent(this, HomeActivity::class.java)
        val unlockWithBiometrics = Intent(this, UnlockWithBiometricsActivity::class.java)
        val sharedPref = getSharedPreferences("prefs", MODE_PRIVATE)
        val isFirstOpen = sharedPref.getBoolean("isFirstOpen", true)
        val useBiometricsUnlock = sharedPref.getBoolean("useBiometricsUnlock", false)

        val continueButton = findViewById<AppCompatButton>(R.id.continue_button)

        continueButton.setOnClickListener {
            sharedPref.edit { putBoolean("isFirstOpen", false) }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && BiometricManager.from(this).canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
            ) {
                startActivity(enableBiometricsUnlockPrompt)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                } else {
                    @Suppress("DEPRECATION")
                    overridePendingTransition(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                }
            } else {
                startActivity(home)
            }
            finish()
        }

        if (!isFirstOpen) {
            if (useBiometricsUnlock) {
                startActivity(unlockWithBiometrics)
            } else {
                startActivity(home)
            }
            finish()
        }
    }
}