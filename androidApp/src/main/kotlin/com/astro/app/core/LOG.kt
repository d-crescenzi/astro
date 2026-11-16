package com.astro.app.core

import android.util.Log
import com.astro.app.BuildConfig

// Debug-gated logging; the only place allowed to call android.util.Log.
@Suppress("ClassNaming")
object LOG {
    fun d(message: String) {
        if (BuildConfig.DEBUG) Log.d(AppConstants.LOG_TAG, message)
    }

    fun e(message: String, error: Throwable? = null) {
        if (BuildConfig.DEBUG) Log.e(AppConstants.LOG_TAG, message, error)
    }
}
