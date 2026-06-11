package com.leopra.runningtrainer.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.leopra.runningtrainer.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.leopra.runningtrainer.domain.model.UserPreferencesDto
import com.leopra.runningtrainer.ui.theme.SurfaceVar
import com.leopra.runningtrainer.ui.theme.TextMuted

@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    preferences: UserPreferencesDto,
    onSave: (name: String, age: String, weightKg: String, heightCm: String, useKilometers: Boolean, claudeApiKey: String, notificationsEnabled: Boolean, notificationHour: Int, notificationMinute: Int, localeCode: String) -> Unit,
    onStartNewPlan: () -> Unit,
    onResetAll: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onBack: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(preferences.name.orEmpty()) }
    var age by rememberSaveable { mutableStateOf(preferences.age?.toString().orEmpty()) }
    var weight by rememberSaveable { mutableStateOf(preferences.weightKg?.toString().orEmpty()) }
    var height by rememberSaveable { mutableStateOf(preferences.heightCm?.toString().orEmpty()) }
    var useKm by rememberSaveable { mutableStateOf(preferences.useKilometers) }
    var apiKey by rememberSaveable { mutableStateOf(preferences.claudeApiKey.orEmpty()) }
    var obscureKey by rememberSaveable { mutableStateOf(true) }
    var notificationsEnabled by rememberSaveable { mutableStateOf(preferences.notificationsEnabled) }
    var notificationHour by rememberSaveable { mutableStateOf(preferences.notificationHour.toString()) }
    var notificationMinute by rememberSaveable { mutableStateOf(preferences.notificationMinute.toString().padStart(2, '0')) }
    var localeCode by rememberSaveable { mutableStateOf(preferences.localeCode) }
    var showNewPlanDialog by rememberSaveable { mutableStateOf(false) }
    var resetConfirmVisible by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Alarms are scheduled regardless; the grant only controls whether they are shown. */ }

    if (showNewPlanDialog) {
        AlertDialog(
            onDismissRequest = { showNewPlanDialog = false },
            title = { Text(stringResource(R.string.dialog_new_plan_title)) },
            text = { Text(stringResource(R.string.dialog_new_plan_body)) },
            confirmButton = {
                TextButton(onClick = { showNewPlanDialog = false; onStartNewPlan() }) {
                    Text(stringResource(R.string.btn_start_new_plan), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPlanDialog = false }) { Text(stringResource(R.string.btn_cancel)) }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (resetConfirmVisible) {
        ResetConfirmDialog(
            onConfirm = { resetConfirmVisible = false; onResetAll() },
            onDismiss = { resetConfirmVisible = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Profile ───────────────────────────────────────────────────────────
        SectionHeader(stringResource(R.string.section_profile))
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsField(value = name, onValueChange = { name = it }, label = stringResource(R.string.field_name))
                SettingsField(
                    value = age, onValueChange = { age = it }, label = stringResource(R.string.field_age),
                    keyboardType = KeyboardType.Number
                )
                SettingsField(
                    value = weight, onValueChange = { weight = it }, label = stringResource(R.string.field_weight_kg),
                    keyboardType = KeyboardType.Decimal
                )
                SettingsField(
                    value = height, onValueChange = { height = it }, label = stringResource(R.string.field_height_cm),
                    keyboardType = KeyboardType.Decimal
                )
            }
        }

        // ── AI Coaching ───────────────────────────────────────────────────────
        SectionHeader(stringResource(R.string.section_ai_coaching))
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.ai_coaching_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                SettingsField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = stringResource(R.string.field_claude_api_key),
                    visualTransformation = if (obscureKey) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = {
                        TextButton(onClick = { obscureKey = !obscureKey }) {
                            Text(
                                if (obscureKey) stringResource(R.string.btn_show) else stringResource(R.string.btn_hide),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }

        // ── Language ──────────────────────────────────────────────────────────
        SectionHeader(stringResource(R.string.section_language))
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val langs = listOf("en" to stringResource(R.string.lang_english),
                                   "it" to stringResource(R.string.lang_italian),
                                   "de" to stringResource(R.string.lang_german))
                langs.forEach { (code, label) ->
                    val selected = localeCode == code
                    val primary = MaterialTheme.colorScheme.primary
                    val shape = RoundedCornerShape(20.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(shape)
                            .background(if (selected) primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant, shape)
                            .border(1.dp, if (selected) primary else SurfaceVar, shape)
                            .clickable { localeCode = code }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, style = MaterialTheme.typography.labelMedium,
                            color = if (selected) primary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // ── Units ─────────────────────────────────────────────────────────────
        SectionHeader(stringResource(R.string.section_units))
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.use_kilometers), style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = useKm,
                    onCheckedChange = { useKm = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.background,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // ── Notifications ─────────────────────────────────────────────────────
        SectionHeader(stringResource(R.string.section_notifications))
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.workout_reminders), style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            notificationsEnabled = enabled
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.background,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                if (notificationsEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsField(
                            value = notificationHour,
                            onValueChange = { notificationHour = it },
                            label = stringResource(R.string.field_hour),
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                        SettingsField(
                            value = notificationMinute,
                            onValueChange = { notificationMinute = it },
                            label = stringResource(R.string.field_minute),
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ── Training Plan ─────────────────────────────────────────────────────
        SectionHeader(stringResource(R.string.section_training_plan))
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.training_plan_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Button(
                    onClick = { showNewPlanDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.btn_start_new_plan))
                }
            }
        }

        // ── About ─────────────────────────────────────────────────────────────
        SectionHeader(stringResource(R.string.section_about))
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.privacy_policy),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPrivacy() }
                    .padding(vertical = 4.dp)
            )
        }

        // ── Save ──────────────────────────────────────────────────────────────
        Button(
            onClick = {
                val hour = notificationHour.toIntOrNull()?.coerceIn(0, 23) ?: 8
                val minute = notificationMinute.toIntOrNull()?.coerceIn(0, 59) ?: 0
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeCode))
                onSave(name, age, weight, height, useKm, apiKey, notificationsEnabled, hour, minute, localeCode)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.btn_save_settings), style = MaterialTheme.typography.labelLarge)
        }

        // ── Destructive action ────────────────────────────────────────────────
        TextButton(
            onClick = { resetConfirmVisible = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(stringResource(R.string.btn_reset_all_data), style = MaterialTheme.typography.labelMedium)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ── Dialogs ───────────────────────────────────────────────────────────────────

@Composable
fun ResetConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_reset_title)) },
        text = { Text(stringResource(R.string.dialog_reset_body)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.btn_reset))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

// ── Private components ────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun SettingsField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val primary = MaterialTheme.colorScheme.primary
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, color = TextMuted, style = MaterialTheme.typography.bodyMedium) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SurfaceVar,
            unfocusedContainerColor = SurfaceVar,
            focusedIndicatorColor = primary,
            unfocusedIndicatorColor = SurfaceVar,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = primary,
            cursorColor = primary
        )
    )
}
