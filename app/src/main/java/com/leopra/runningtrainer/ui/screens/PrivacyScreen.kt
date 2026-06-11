package com.leopra.runningtrainer.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.leopra.runningtrainer.R
import com.leopra.runningtrainer.ui.theme.TextMuted

@Composable
fun PrivacyScreen(
    innerPadding: PaddingValues,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        PrivacySection(
            title = "Data Storage",
            body = "Running Trainer stores all your data locally on your device. " +
                "Training plans, workout logs, and personal profile information " +
                "never leave your device unless you explicitly share them."
        )
        PrivacySection(
            title = "Encrypted Data",
            body = "Your profile data (name, age, weight, height) is stored on device. " +
                "The Claude API key is stored in plain text in local preferences — " +
                "do not use this app on a shared or rooted device if you care about key secrecy."
        )
        PrivacySection(
            title = "Claude AI (Optional)",
            body = "If you provide a Claude API key, workout data is sent to " +
                "Anthropic's API to generate coaching descriptions and post-workout " +
                "feedback. This is entirely optional — the app works fully offline " +
                "without an API key. Refer to Anthropic's privacy policy for how they handle API data."
        )
        PrivacySection(
            title = "No Accounts or Tracking",
            body = "Running Trainer requires no account, login, or registration. " +
                "No analytics, crash reporting, or usage tracking is collected. " +
                "No third-party SDKs with tracking capabilities are included."
        )
        PrivacySection(
            title = "Notifications",
            body = "Workout reminders are scheduled locally on your device. " +
                "No notification data is sent to external servers."
        )
        PrivacySection(
            title = "Data Deletion",
            body = "You can delete all app data at any time via Settings → Reset All Data. " +
                "Uninstalling the app removes all locally stored data."
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.privacy_last_updated),
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun PrivacySection(title: String, body: String) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
}
