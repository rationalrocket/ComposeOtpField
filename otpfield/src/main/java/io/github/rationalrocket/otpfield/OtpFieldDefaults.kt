package io.github.rationalrocket.otpfield

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Contains default styling properties and shapes for [OtpField] and related OTP components.
 */
object OtpFieldDefaults {

    /**
     * Default shape for OTP input boxes.
     */
    val Shape: Shape = RoundedCornerShape(12.dp)

    /**
     * Default border color for a focused OTP box.
     */
    @Composable
    fun focusedBorderColor(): Color =
        MaterialTheme.colorScheme.primary

    /**
     * Default border color for an unfocused OTP box.
     */
    @Composable
    fun unfocusedBorderColor(): Color =
        MaterialTheme.colorScheme.outline

    /**
     * Default container background color for OTP boxes.
     */
    @Composable
    fun containerColor(): Color =
        MaterialTheme.colorScheme.surface
}
