package io.github.rationalrocket.composeotpfield.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.rationalrocket.otpfield.AdvancedOtpField

@Composable
fun AdvancedOtpScreen(modifier: Modifier = Modifier) {
    val otpLength = 6
    val otpValues = remember(otpLength) { mutableStateListOf(*Array(otpLength) { "" }) }
    var isError by remember { mutableStateOf(false) }
    var obscureText by remember { mutableStateOf(false) }
    var verificationStatus by remember { mutableStateOf<VerificationStatus>(VerificationStatus.Idle) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Advanced OTP Field",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hint: Correct OTP is 123456",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // OTP Input Component
        AdvancedOtpField(
            otpLength = otpLength,
            otpValues = otpValues,
            isError = isError,
            obscureText = obscureText,
            errorMessage = "Incorrect OTP! Try 123456",
            showErrorToast = true,
            onError = { errorMsg ->
                Log.d("AdvancedOtpField", "Error occurred: $errorMsg")
            },
            onUpdateOtpValuesByIndex = { index, value ->
                isError = false
                verificationStatus = VerificationStatus.Idle
                otpValues[index] = value
            },
            onOtpInputComplete = {
                val entered = otpValues.joinToString("")
                if (entered == "123456") {
                    isError = false
                    verificationStatus = VerificationStatus.Success(entered)
                    Log.d("OTP", "Advanced OTP Correct: $entered")
                } else {
                    isError = true
                    verificationStatus = VerificationStatus.Error("Incorrect OTP! Try 123456")
                    Log.d("OTP", "Advanced OTP Incorrect: $entered")
                }
            }
        )

        // Status / Completion
        when (val status = verificationStatus) {
            is VerificationStatus.Success -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Successfully Verified OTP: ${status.otp}",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            is VerificationStatus.Error -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = status.message,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            is VerificationStatus.Idle -> {}
        }

        Spacer(modifier = Modifier.weight(1f))

        // Controls / Options Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Interactive Controls",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Obscure Text (Password)")
                    Switch(
                        checked = obscureText,
                        onCheckedChange = { obscureText = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isError = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Trigger Error")
                    }

                    OutlinedButton(
                        onClick = {
                            for (i in otpValues.indices) otpValues[i] = ""
                            verificationStatus = VerificationStatus.Idle
                            isError = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset")
                    }
                }
            }
        }
    }
}
