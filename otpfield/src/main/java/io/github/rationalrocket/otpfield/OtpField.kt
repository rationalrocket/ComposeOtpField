package io.github.rationalrocket.otpfield

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

/**
 * A standard, streamlined OTP input field composable using a basic text field
 * with decorated boxes representing each digit.
 *
 * @param otpLength The total number of digits in the OTP field. Defaults to 6.
 * @param onOtpComplete Callback invoked with the complete OTP string when all digits are entered.
 */
@Composable
fun OtpField(
    otpLength: Int = 6,
    shape: Shape = OtpFieldDefaults.Shape,
    containerColor: Color = OtpFieldDefaults.containerColor(),
    focusedBorderColor: Color = OtpFieldDefaults.focusedBorderColor(),
    unfocusedBorderColor: Color = OtpFieldDefaults.unfocusedBorderColor(),
    onOtpComplete: (String) -> Unit
) {
    var otpValue by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    BasicTextField(
        value = otpValue,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() }
            if (filtered.length <= otpLength) {
                otpValue = filtered
                if (otpValue.length == otpLength) {
                    keyboardController?.hide()
                    onOtpComplete(otpValue)
                }
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        decorationBox = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(otpLength) { index ->
                    val char = when {
                        index < otpValue.length -> otpValue[index].toString()
                        else -> ""
                    }
                    val isFocused = otpValue.length == index

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .background(
                                color = containerColor,
                                shape = shape
                            )
                            .border(
                                width = 2.dp,
                                color = if (isFocused) focusedBorderColor else unfocusedBorderColor.copy(alpha = 0.4f),
                                shape = shape
                            )
                            .clickable {
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            }
                            .semantics {
                                contentDescription = "OTP digit ${index + 1} of $otpLength"
                                stateDescription = if (char.isEmpty()) "Empty" else "Filled: $char"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    )

    // Auto-focus hidden field when first shown
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}

@Preview(showBackground = true)
@Composable
fun OTPInputViewFullPreview() {
    MaterialTheme {
        OtpField(onOtpComplete = {})
    }
}
