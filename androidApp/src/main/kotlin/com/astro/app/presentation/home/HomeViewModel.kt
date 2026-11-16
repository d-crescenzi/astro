package com.astro.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astro.app.core.states.ResultState
import com.astro.app.data.SessionStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// Home shows how the last test went: its cognitive score. No record yet reads as 0.
data class HomeUi(val readiness: Int, val hasResult: Boolean)

class HomeViewModel(store: SessionStore) : ViewModel() {

    val state: StateFlow<ResultState<HomeUi>> =
        store.sessions.map { sessions ->
            val score = sessions.lastOrNull()?.cognitiveScore
            ResultState.Success(HomeUi(readiness = score ?: 0, hasResult = score != null)) as ResultState<HomeUi>
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ResultState.Idle)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
