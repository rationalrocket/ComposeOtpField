package io.github.rationalrocket.composeotpfield.ui.screens

sealed class VerificationStatus {
    object Idle : VerificationStatus()
    data class Success(val otp: String) : VerificationStatus()
    data class Error(val message: String) : VerificationStatus()
}
