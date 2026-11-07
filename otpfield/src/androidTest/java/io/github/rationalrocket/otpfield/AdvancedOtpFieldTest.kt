package io.github.rationalrocket.otpfield

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdvancedOtpFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testTypingDigitsAdvancesFocus() {
        var otpValues = listOf("", "", "", "")
        composeTestRule.setContent {
            var values by remember { mutableStateOf(listOf("", "", "", "")) }
            AdvancedOtpField(
                otpLength = 4,
                otpValues = values,
                onUpdateOtpValuesByIndex = { index, value ->
                    val mutable = values.toMutableList()
                    mutable[index] = value
                    values = mutable
                    otpValues = values
                },
                onOtpInputComplete = {}
            )
        }

        // Type '5' into the first digit box
        composeTestRule.onNodeWithContentDescription("OTP digit 1 of 4").performTextInput("5")
        composeTestRule.waitForIdle()

        assertEquals("5", otpValues[0])
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testBackspaceClearsAndMovesBack() {
        var otpValues = listOf("5", "", "", "")
        composeTestRule.setContent {
            var values by remember { mutableStateOf(listOf("5", "", "", "")) }
            AdvancedOtpField(
                otpLength = 4,
                otpValues = values,
                onUpdateOtpValuesByIndex = { index, value ->
                    val mutable = values.toMutableList()
                    mutable[index] = value
                    values = mutable
                    otpValues = values
                },
                onOtpInputComplete = {}
            )
        }

        // Focus second box and press Backspace on empty second box to clear first box and move back
        composeTestRule.onNodeWithContentDescription("OTP digit 2 of 4").performTextInput("")
        composeTestRule.onNodeWithContentDescription("OTP digit 2 of 4").performKeyInput {
            pressKey(Key.Backspace)
        }
        composeTestRule.waitForIdle()

        assertEquals("", otpValues[0])
    }

    @Test
    fun testErrorStateTriggers() {
        var errorCallbackTriggered = false
        var errorMessageReceived = ""

        composeTestRule.setContent {
            var isError by remember { mutableStateOf(false) }
            val values = remember { mutableStateOf(listOf("", "", "", "")) }

            AdvancedOtpField(
                otpLength = 4,
                otpValues = values.value,
                isError = isError,
                errorMessage = "Invalid OTP Code",
                showErrorToast = false,
                onError = { msg ->
                    errorCallbackTriggered = true
                    errorMessageReceived = msg
                },
                onUpdateOtpValuesByIndex = { index, value ->
                    val mutable = values.value.toMutableList()
                    mutable[index] = value
                    values.value = mutable
                },
                onOtpInputComplete = {}
            )

            // Trigger error
            androidx.compose.runtime.LaunchedEffect(Unit) {
                isError = true
            }
        }

        composeTestRule.waitForIdle()
        assertEquals(true, errorCallbackTriggered)
        assertEquals("Invalid OTP Code", errorMessageReceived)
    }
}
