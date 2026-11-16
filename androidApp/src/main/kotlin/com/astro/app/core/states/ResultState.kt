package com.astro.app.core.states

// Presentation-only wrapper; every ViewModel exposes one, starting Idle.
sealed interface ResultState<out T> {
    data object Idle : ResultState<Nothing>
    data object Loading : ResultState<Nothing>
    data class Success<T>(val data: T) : ResultState<T>
    data class Failure(val message: String) : ResultState<Nothing>
}
