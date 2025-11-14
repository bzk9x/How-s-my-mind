package com.howsmymind.core.bg

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.core.content.edit
import com.howsmymind.activities.UnlockWithBiometricsActivity

object BackgroundActivityTrigger : DefaultLifecycleObserver {

    private const val PREFS_NAME = "bg_trigger_prefs"
    private const val KEY_BACKGROUND_TIME = "background_time"
    private const val MIN_BACKGROUND_TIME = 60_000L
    private const val KEY_USE_BIOMETRICS = "useBiometricsUnlock"

    private lateinit var appContext: Context
    private var backgroundTime: Long = 0
    private var isUnlockActivityLaunched = false
    private var hasCheckedAfterBackground = false

    @RequiresApi(Build.VERSION_CODES.Q)
    fun init(application: Application) {
        appContext = application.applicationContext
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {
                checkAndTriggerUnlock(activity)
            }
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (activity is UnlockWithBiometricsActivity) {
                    isUnlockActivityLaunched = false
                }
            }
        })
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        hasCheckedAfterBackground = false
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)

        if (!isBiometricsEnabled()) {
            return
        }

        backgroundTime = System.currentTimeMillis()
        isUnlockActivityLaunched = false

        getPrefs().edit {
            putLong(KEY_BACKGROUND_TIME, backgroundTime)
        }
    }

    private fun checkAndTriggerUnlock(activity: Activity) {
        if (!isBiometricsEnabled()) {
            return
        }

        if (activity is UnlockWithBiometricsActivity ||
            isUnlockActivityLaunched ||
            hasCheckedAfterBackground) {
            return
        }

        val prefs = getPrefs()
        val savedBackgroundTime = prefs.getLong(KEY_BACKGROUND_TIME, 0)

        if (savedBackgroundTime > 0) {
            val timeInBackground = System.currentTimeMillis() - savedBackgroundTime

            hasCheckedAfterBackground = true

            prefs.edit { remove(KEY_BACKGROUND_TIME) }

            if (timeInBackground >= MIN_BACKGROUND_TIME) {
                launchUnlockActivity(activity)
            }
        }
    }

    private fun launchUnlockActivity(currentActivity: Activity) {
        if (isUnlockActivityLaunched || !isBiometricsEnabled()) {
            return
        }

        try {
            isUnlockActivityLaunched = true

            val intent = Intent(currentActivity, UnlockWithBiometricsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("triggered_from_background", true)
            }

            currentActivity.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            isUnlockActivityLaunched = false
        }
    }

    private fun isBiometricsEnabled(): Boolean {
        return try {
            val sharedPref = appContext.getSharedPreferences("your_app_prefs", Context.MODE_PRIVATE)
            sharedPref.getBoolean(KEY_USE_BIOMETRICS, false)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getPrefs() = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}