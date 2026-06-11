package com.leopra.runningtrainer.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.leopra.runningtrainer.R
import com.leopra.runningtrainer.domain.model.PaceZone
import com.leopra.runningtrainer.domain.model.Workout
import com.leopra.runningtrainer.domain.model.WorkoutFeeling
import com.leopra.runningtrainer.ui.WorkoutLogUiState
import com.leopra.runningtrainer.ui.theme.SurfaceVar
import com.leopra.runningtrainer.ui.theme.TextMuted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutDetailScreen(
    innerPadding: PaddingValues,
    workout: Workout?,
    paceZones: List<PaceZone> = emptyList(),
    workoutLogUiState: WorkoutLogUiState = WorkoutLogUiState(),
    onSave: (workoutId: String, distance: String, duration: String, notes: String, rpe: Int?, feeling: WorkoutFeeling?) -> Unit,
    onClear: (String) -> Unit,
    onCoachingDismissed: () -> Unit = {},
    onOpenStretching: ((isPreRun: Boolean) -> Unit)? = null,
    onBack: () -> Unit
) {
    if (workout == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.workout_not_found), style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onBack, shape = RoundedCornerShape(12.dp)) { Text(stringResource(R.string.btn_back)) }
        }
        return
    }

    val typeColor = workoutTypeColor(workout.type)

    var distance by rememberSaveable(workout.id) { mutableStateOf(workout.actualDistanceKm?.toString().orEmpty()) }
    var duration by rememberSaveable(workout.id) { mutableStateOf(workout.actualDurationMinutes?.toString().orEmpty()) }
    var notes by rememberSaveable(workout.id) { mutableStateOf(workout.notes.orEmpty()) }
    var rpe by rememberSaveable(workout.id) { mutableIntStateOf(workout.rpe ?: 5) }
    var rpeSelected by rememberSaveable(workout.id) { mutableStateOf(workout.rpe != null) }
    var feeling by rememberSaveable(workout.id) { mutableStateOf(workout.feeling) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header with type color accent
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(typeColor)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(workout.title, style = MaterialTheme.typography.headlineMedium)
                Text(
                    stringResource(R.string.home_day_label, workout.dayOfWeek) + " · " + workout.type.typeLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        }

        // Planned reference
        val plannedRef = buildString {
            workout.distanceKm?.let { append("Planned ${"%.1f".format(it)} km") }
            workout.durationMinutes?.let {
                if (isNotEmpty()) append("  ·  ")
                append("~$it min")
            }
        }
        if (plannedRef.isNotEmpty()) {
            Text(plannedRef, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        }

        // Pace zone card
        if (paceZones.isNotEmpty()) {
            val shape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface, shape)
                    .border(1.dp, typeColor.copy(alpha = 0.3f), shape)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.target_pace_zone), style = MaterialTheme.typography.titleSmall, color = typeColor)
                    paceZones.forEach { zone ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(zone.type.typeLabel(), style = MaterialTheme.typography.bodyMedium)
                            Text(zone.paceRange, style = MaterialTheme.typography.labelLarge, color = typeColor)
                        }
                        Text(zone.type.zoneDescription(), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }
        }

        // AI coach's note (from week enrichment)
        if (!workout.description.isNullOrBlank()) {
            val shape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface, shape)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), shape)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.workout_coach_note),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(workout.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // AI coaching tip (from week enrichment)
        if (!workout.coachingTip.isNullOrBlank()) {
            val shape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f), shape)
                    .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f), shape)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.workout_coaching_tip),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(workout.coachingTip, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Distance validation warning
        val distanceDouble = distance.toDoubleOrNull()
        val plannedKm = workout.distanceKm
        if (distanceDouble != null && plannedKm != null && plannedKm > 0 && distanceDouble > plannedKm * 3) {
            Text(
                stringResource(R.string.workout_note_high_distance, plannedKm),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        // Log fields
        DetailField(
            value = distance, onValueChange = { distance = it },
            label = stringResource(R.string.field_actual_distance), keyboardType = KeyboardType.Decimal
        )
        DetailField(
            value = duration, onValueChange = { duration = it },
            label = stringResource(R.string.field_actual_duration), keyboardType = KeyboardType.Number
        )
        DetailField(value = notes, onValueChange = { notes = it }, label = stringResource(R.string.field_notes))

        // RPE
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.perceived_effort), style = MaterialTheme.typography.titleMedium)
                Text(
                    if (rpeSelected) stringResource(R.string.rpe_value, rpe) else stringResource(R.string.rpe_not_set),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (rpeSelected) typeColor else TextMuted
                )
            }
            Slider(
                value = rpe.toFloat(),
                onValueChange = { rpe = it.toInt(); rpeSelected = true },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = typeColor,
                    activeTrackColor = typeColor,
                    inactiveTrackColor = SurfaceVar
                )
            )
        }

        // Feeling
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.how_did_you_feel), style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkoutFeeling.entries.forEach { item ->
                    FeelingChip(
                        feeling = item,
                        selected = feeling == item,
                        onClick = { feeling = if (feeling == item) null else item }
                    )
                }
            }
        }

        // Actions
        Button(
            onClick = { onSave(workout.id, distance, duration, notes, if (rpeSelected) rpe else null, feeling) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = typeColor, contentColor = Color.Black)
        ) {
            Text(
                if (workout.isCompleted) stringResource(R.string.btn_update_log) else stringResource(R.string.btn_mark_done),
                style = MaterialTheme.typography.labelLarge
            )
        }
        if (workout.isCompleted) {
            Button(
                onClick = { onClear(workout.id) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.btn_clear_log))
            }
        }
        // Post-workout AI coaching — streaming section (shown immediately after saving)
        val showStreaming = workoutLogUiState.currentWorkoutId == workout.id && (
            workoutLogUiState.isStreaming ||
            workoutLogUiState.streamingCoaching.isNotBlank() ||
            workoutLogUiState.coachingAuthError
        )
        if (showStreaming) {
            val shape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), shape)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), shape)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.workout_post_coaching),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    when {
                        workoutLogUiState.coachingAuthError -> Text(
                            stringResource(R.string.coaching_auth_error),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        else -> {
                            val cursorAlpha by rememberInfiniteTransition(label = "cursor")
                                .animateFloat(
                                    initialValue = 1f,
                                    targetValue = 0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(durationMillis = 500),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "cursorAlpha"
                                )
                            val displayText = if (workoutLogUiState.isStreaming) {
                                buildAnnotatedString {
                                    append(workoutLogUiState.streamingCoaching)
                                    withStyle(SpanStyle(color = LocalContentColor.current.copy(alpha = cursorAlpha))) {
                                        append("|")
                                    }
                                }
                            } else {
                                AnnotatedString(workoutLogUiState.streamingCoaching)
                            }
                            Text(text = displayText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            if (!workoutLogUiState.isStreaming) {
                Button(
                    onClick = onCoachingDismissed,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.btn_continue))
                }
            }
        }

        // Post-workout AI coaching — static section (re-opened completed workout, no active stream)
        if (!showStreaming && workout.isCompleted && !workout.postWorkoutCoaching.isNullOrBlank()) {
            val shape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), shape)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), shape)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.workout_post_coaching),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(workout.postWorkoutCoaching, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Stretching
        if (onOpenStretching != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onOpenStretching(true) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text(stringResource(R.string.btn_pre_run_stretch)) }
                OutlinedButton(
                    onClick = { onOpenStretching(false) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text(stringResource(R.string.btn_post_run_stretch)) }
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
    }
}

// ── Private components ────────────────────────────────────────────────────────

@Composable
private fun DetailField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val primary = MaterialTheme.colorScheme.primary
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, color = TextMuted) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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

@Composable
private fun FeelingChip(feeling: WorkoutFeeling, selected: Boolean, onClick: () -> Unit) {
    val label = when (feeling) {
        WorkoutFeeling.great   -> stringResource(R.string.feeling_great)
        WorkoutFeeling.good    -> stringResource(R.string.feeling_good)
        WorkoutFeeling.ok      -> stringResource(R.string.feeling_ok)
        WorkoutFeeling.tired   -> stringResource(R.string.feeling_tired)
        WorkoutFeeling.injured -> stringResource(R.string.feeling_injured)
    }
    val shape = RoundedCornerShape(20.dp)
    val primary = MaterialTheme.colorScheme.primary
    val bg = if (selected) primary.copy(alpha = 0.15f) else SurfaceVar.copy(alpha = 0.5f)
    val border = if (selected) primary else SurfaceVar

    Box(
        modifier = Modifier
            .clip(shape)
            .background(bg, shape)
            .border(1.dp, border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
