package com.example.learnverse.utils

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

// This object will broadcast logout events across the app.
object SessionManager {
    // A channel is used to send a one-time event.
    private val _logoutChannel = Channel<Unit>(Channel.CONFLATED)
    val logoutEvent = _logoutChannel.receiveAsFlow()

    // A non-suspending function to trigger the logout from anywhere.
    fun triggerLogout() {
        _logoutChannel.trySend(Unit)
    }
}