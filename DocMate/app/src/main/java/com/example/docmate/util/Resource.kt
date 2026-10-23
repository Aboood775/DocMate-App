package com.example.docmate.util

/**
 * A generic class that holds a value with its loading status.
 * Used for managing UI states in ViewModel and UI layers.
 */
sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val exception: Throwable) : Resource<Nothing>()
    object Empty : Resource<Nothing>()
}
