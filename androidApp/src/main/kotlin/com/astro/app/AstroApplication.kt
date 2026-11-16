package com.astro.app

import android.app.Application
import com.astro.app.core.di.appModule
import com.astro.app.data.reminders.Reminders
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AstroApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AstroApplication)
            modules(appModule)
        }
        Reminders.createChannel(this)
        Reminders.scheduleNext(this)
    }
}
