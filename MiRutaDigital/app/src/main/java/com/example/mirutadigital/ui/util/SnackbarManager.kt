package com.example.mirutadigital.ui.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object SnackbarManager {
    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    suspend fun showMessage(message: String) {
        _messages.emit(message)
    }
}
