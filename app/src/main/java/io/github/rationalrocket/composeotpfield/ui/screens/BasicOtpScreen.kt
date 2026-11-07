package io.github.rationalrocket.composeotpfield.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.rationalrocket.otpfield.OtpField

@Composable
fun BasicOtpScreen(modifier: Modifier = Modifier) {
    var verificationStatus by remember { mutableStateOf<VerificationStatus>(VerificationStatus.Idle) }
    var resetKey by remember { mutableIntStateOf(0) }

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
                text = "Basic OTP Field",
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

        Spacer(modifier = Modifier.height(24.dp))

        // Keyed to allow resetting
        key(resetKey) {
            OtpField(
                otpLength = 6,
                onOtpComplete = { otp ->
                    if (otp == "123456") {
                        verificationStatus = VerificationStatus.Success(otp)
                        Log.d("OTP", "Basic OTP Correct: $otp")
                    } else {
                        verificationStatus = VerificationStatus.Error("Incorrect OTP! Try 123456")
                        Log.d("OTP", "Basic OTP Incorrect: $otp")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status / Completion
        when (val status = verificationStatus) {
            is VerificationStatus.Success -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "OTP Completed: ${status.otp}",
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

        OutlinedButton(
            onClick = {
                verificationStatus = VerificationStatus.Idle
                resetKey++
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset OTP Field")
        }
    }
}
