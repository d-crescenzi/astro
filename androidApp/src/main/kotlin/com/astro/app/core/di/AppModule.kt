package com.astro.app.core.di

import android.os.SystemClock
import com.astro.app.assessment.DefaultFitForDutyEngine
import com.astro.app.assessment.FitForDutyEngine
import com.astro.app.data.SessionStore
import com.astro.app.data.sensors.HeartRateSource
import com.astro.app.data.sensors.VitalsMonitor
import com.astro.app.presentation.home.HomeViewModel
import com.astro.app.presentation.test_flow.TestFlowViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// Manual Koin wiring (no annotations, no codegen).
val appModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { SessionStore(androidContext()) }
    single { HeartRateSource(androidContext()) }
    single { VitalsMonitor(heartRate = get(), scope = get(), clock = SystemClock::elapsedRealtime) }
    // Fresh seed per session: reproducible within a run, varied across runs.
    factory<FitForDutyEngine> { DefaultFitForDutyEngine(seed = System.currentTimeMillis()) }

    viewModel { HomeViewModel(store = get()) }
    viewModel { TestFlowViewModel(store = get(), vitals = get(), engineFactory = { get() }, context = androidContext()) }
}
