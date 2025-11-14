package com.howsmymind

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.howsmymind.core.auth.BiometricAuth
import com.howsmymind.core.haptics.ErrorRumble

class UnlockWithBiometricsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_unlock_with_biometrics)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val home = Intent(this, HomeActivity::class.java)
        val errorRumble = ErrorRumble(this)
        val biometricAuth = BiometricAuth(activity = this, onSuccess = {
            startActivity(home)
            finish()
        }, onFailure = {}, onError = {
            errorRumble.trigger()
        })

        val unlock = findViewById<TextView>(R.id.unlock)

        unlock.setOnClickListener {
            biometricAuth.show()
        }
    }
}