package com.howsmymind

import android.app.Application
import android.os.Build
import com.howsmymind.core.bg.BackgroundActivityTrigger

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            BackgroundActivityTrigger.init(this)
        }
    }
}