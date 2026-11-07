package io.github.rationalrocket.otpfield


import android.content.Context
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.github.rationalrocket.otpfield.util.showToast
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource

/**
 * An advanced, highly customizable OTP (One-Time Password) input field composable.
 * Supports multi-digit entry, auto-focus, paste handling, backspace navigation,
 * error shaking animation, error messaging, optional toast notifications, and accessibility semantics.
 *
 * @param otpLength The total number of digits/boxes in the OTP field.
 * @param onUpdateOtpValuesByIndex Callback invoked when the digit at a specific index is updated.
 * @param onOtpInputComplete Callback invoked when all digits have been entered and input is complete.
 * @param modifier Modifier to be applied to the layout.
 * @param otpValues The current list of OTP digit strings.
 * @param isError Whether the OTP field is currently in an error state.
 * @param fontSize The font size of the text inside each OTP box.
 * @param obscureText Whether to obscure the entered text (e.g., for password/PIN fields).
 * @param autoFocus Whether to automatically request focus on the first box when displayed.
 * @param errorMessage Optional error message associated with the error state.
 * @param showErrorToast Whether to show a toast message when an error occurs. Defaults to `false`.
 * @param onError Optional callback invoked when an error occurs, receiving the error message.
 */
@Composable
fun AdvancedOtpField(
    otpLength: Int,
    onUpdateOtpValuesByIndex: (Int, String) -> Unit,
    onOtpInputComplete: () -> Unit,
    modifier: Modifier = Modifier,
    otpValues: List<String> = List(otpLength) { "" },
    isError: Boolean = false,
    fontSize: TextUnit = MaterialTheme.typography.bodyLarge.fontSize,
    obscureText: Boolean = false,
    autoFocus: Boolean = true,
    errorMessage: String? = null,
    showErrorToast: Boolean = false,
    onError: ((String) -> Unit)? = null,
    shape: Shape = OtpFieldDefaults.Shape,
    focusedBorderColor: Color = OtpFieldDefaults.focusedBorderColor(),
    unfocusedBorderColor: Color = OtpFieldDefaults.unfocusedBorderColor(),
    containerColor: Color = OtpFieldDefaults.containerColor()
) {
    val focusRequesters = remember { List(otpLength) { FocusRequester() } }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val vibrator = remember(context) { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    val scope = rememberCoroutineScope()

    var activeIndex by remember { mutableIntStateOf(0) }
    var isMovingFocus by remember { mutableStateOf(false) }

    val shake = remember { Animatable(0f) }
    val errMsg= stringResource(R.string.invalid_input_tc)

    LaunchedEffect(isError) {
        if (isError) {
            vibrator.vibrate(200)
            shake.snapTo(0f)
            shake.animateTo(10f, tween(20))
            shake.animateTo(-10f, tween(20))
            shake.animateTo(0f, tween(20))

            val msg = errorMessage ?: errMsg
            onError?.invoke(msg)

            if (showErrorToast) {
                context.showToast(msg)
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(translationX = shake.value),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        otpValues.forEachIndexed { index, value ->
            OutlinedTextField(
                visualTransformation = if (obscureText) PasswordVisualTransformation() else VisualTransformation.None,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .focusRequester(focusRequesters[index])
                    .onFocusChanged { state ->
                        if (state.isFocused) {
                            activeIndex = index
                        }
                    }
                    .semantics {
                        contentDescription = "OTP digit ${index + 1} of $otpLength"
                        stateDescription = if (value.isEmpty()) "Empty" else if (obscureText) "Filled" else "Filled: $value"
                        if (isError) {
                            error(errorMessage ?: "Invalid input")
                        }
                    }
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.key == Key.Backspace) {
                            if (value.isEmpty() && index > 0) {
                                onUpdateOtpValuesByIndex(index, "")
                                scope.launch {
                                    Thread.yield()
                                    if (activeIndex == index && !isMovingFocus) {
                                        isMovingFocus = true
                                        focusRequesters[index - 1].requestFocus()
                                        activeIndex = index - 1
                                        isMovingFocus = false
                                    }
                                }
                            } else {
                                onUpdateOtpValuesByIndex(index, "")
                            }
                            true
                        } else {
                            false
                        }
                    },

                value = value,
                onValueChange = { newValue ->
                    // 1. Always filter to digits only (removes . , - and spaces)
                    val digitsOnly = newValue.filter { it.isDigit() }

                    if (digitsOnly.length > 1) {

                        // User is replacing an existing digit, not pasting.
                        if (value.isNotEmpty() && digitsOnly.length == 2) {

                            val replacement = digitsOnly
                                .firstOrNull { it.toString() != value }
                                ?: digitsOnly.last()

                            onUpdateOtpValuesByIndex(index, replacement.toString())

                            if (index < otpLength - 1) {
                                scope.launch {
                                    Thread.yield()
                                    if (activeIndex == index && !isMovingFocus) {
                                        isMovingFocus = true
                                        focusRequesters[index + 1].requestFocus()
                                        activeIndex = index + 1
                                        isMovingFocus = false
                                    }
                                }
                            } else {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onOtpInputComplete()
                            }

                        } else {
                            // Real paste
                            focusManager.clearFocus()

                            val pasteData = digitsOnly.take(otpLength)

                            for (i in 0 until otpLength) {
                                val charToUpdate =
                                    if (i < pasteData.length) pasteData[i].toString() else ""
                                onUpdateOtpValuesByIndex(i, charToUpdate)
                            }

                            if (pasteData.length == otpLength) {
                                keyboardController?.hide()
                                onOtpInputComplete()
                            }
                        }

                    } else {
                        // 3. SINGLE DIGIT / BACKSPACE LOGIC
                        // We use digitsOnly here (which is either 1 digit or empty)
                        onUpdateOtpValuesByIndex(index, digitsOnly)

                        if (digitsOnly.isNotEmpty()) {
                            if (index < otpLength - 1) {
                                scope.launch {
                                    Thread.yield() // Wait for recomposition
                                    if (activeIndex == index && !isMovingFocus) {
                                        isMovingFocus = true
                                        focusRequesters[index + 1].requestFocus()
                                        activeIndex = index + 1
                                        isMovingFocus = false
                                    }
                                }
                            } else {
                                // Finalize on the last box
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onOtpInputComplete()
                            }
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (index == otpLength - 1) {
                        if (otpValues.all { it.isNotEmpty() }) {
                            ImeAction.Done
                        } else {
                            ImeAction.None
                        }
                    } else {
                        ImeAction.Next
                    }
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (index < otpLength - 1) {
                            scope.launch {
                                Thread.yield()
                                if (activeIndex == index && !isMovingFocus) {
                                    isMovingFocus = true
                                    focusRequesters[index + 1].requestFocus()
                                    activeIndex = index + 1
                                    isMovingFocus = false
                                }
                            }
                        }
                    },
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onOtpInputComplete()
                    }
                ),
                shape = shape,
                isError = isError,
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = fontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Color.Transparent,
                    focusedBorderColor = focusedBorderColor,
                    unfocusedBorderColor = unfocusedBorderColor,
                    focusedContainerColor = containerColor,
                    unfocusedContainerColor = containerColor,
                    errorContainerColor = containerColor,
                    focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    errorTextColor = MaterialTheme.colorScheme.error,
                ),
            )
        }
    }



    LaunchedEffect(otpValues, autoFocus) {
        if (autoFocus && otpValues.all { it.isEmpty() }) {
            focusRequesters.firstOrNull()?.requestFocus()
            activeIndex = 0
        }
    }


}

@Preview(showBackground = true)
@Composable
fun AdvancedOtpFieldPreview() {
    MaterialTheme {
        AdvancedOtpField(
            otpLength = 4,
            otpValues = listOf("1", "2", "", ""),
            onUpdateOtpValuesByIndex = { _, _ -> },
            onOtpInputComplete = {})
    }
}
