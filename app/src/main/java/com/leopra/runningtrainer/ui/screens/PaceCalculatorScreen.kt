package com.leopra.runningtrainer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.leopra.runningtrainer.R
import com.leopra.runningtrainer.domain.model.GoalType
import com.leopra.runningtrainer.domain.model.PaceZone
import com.leopra.runningtrainer.domain.model.TrainingPlan
import com.leopra.runningtrainer.domain.model.WorkoutType
import com.leopra.runningtrainer.domain.service.PaceCalculatorService
import com.leopra.runningtrainer.ui.theme.SurfaceVar
import com.leopra.runningtrainer.ui.theme.TextMuted

@Composable
fun PaceCalculatorScreen(
    innerPadding: PaddingValues,
    activePlan: TrainingPlan?,
    savedGoalTimeSeconds: Int?,
    onSaveGoalTime: (Int) -> Unit
) {
    val service = remember { PaceCalculatorService() }

    // Pre-fill goal type from active plan, fall back to 10K
    var selectedGoal by rememberSaveable {
        mutableStateOf(activePlan?.goalType ?: GoalType.tenK)
    }

    // Pre-fill time fields from saved preference
    var hours by rememberSaveable {
        mutableStateOf(savedGoalTimeSeconds?.let { it / 3600 }?.toString().orEmpty())
    }
    var minutes by rememberSaveable {
        mutableStateOf(savedGoalTimeSeconds?.let { (it % 3600) / 60 }?.toString().orEmpty())
    }
    var seconds by rememberSaveable {
        mutableStateOf(savedGoalTimeSeconds?.let { it % 60 }?.toString().orEmpty())
    }
    var zones by rememberSaveable { mutableStateOf(listOf<PaceZone>()) }

    // Recalculate zones whenever inputs change
    LaunchedEffect(selectedGoal, hours, minutes, seconds) {
        val h = hours.toIntOrNull() ?: 0
        val m = minutes.toIntOrNull() ?: 0
        val s = seconds.toIntOrNull() ?: 0
        val total = h * 3600 + m * 60 + s
        val computed = service.calculate(selectedGoal, total)
        zones = computed
        if (computed.isNotEmpty()) onSaveGoalTime(total)
    }

    val distKm = PaceCalculatorService.distanceKm(selectedGoal)
    val distLabel = if (distKm == distKm.toLong().toDouble()) "${distKm.toLong()} km"
                    else "${"%.1f".format(distKm)} km"

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Goal type selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.pace_race_distance),
                    style = MaterialTheme.typography.titleLarge
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(GoalType.entries) { goal ->
                        GoalChip(
                            goal = goal,
                            selected = goal == selectedGoal,
                            onClick = { selectedGoal = goal }
                        )
                    }
                }
            }
        }

        // Goal time input
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.pace_goal_time),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    stringResource(R.string.pace_goal_time_desc, distLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    TimeField(
                        value = hours,
                        onValueChange = { v ->
                            if (v.isEmpty() || (v.toIntOrNull() ?: -1) in 0..9) hours = v
                        },
                        label = stringResource(R.string.pace_hours),
                        modifier = Modifier.weight(1f)
                    )
                    TimeSeparator()
                    TimeField(
                        value = minutes,
                        onValueChange = { v ->
                            if (v.isEmpty() || (v.toIntOrNull() ?: -1) in 0..59) minutes = v
                        },
                        label = stringResource(R.string.pace_minutes),
                        modifier = Modifier.weight(1f)
                    )
                    TimeSeparator()
                    TimeField(
                        value = seconds,
                        onValueChange = { v ->
                            if (v.isEmpty() || (v.toIntOrNull() ?: -1) in 0..59) seconds = v
                        },
                        label = stringResource(R.string.pace_seconds),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Training zones
        if (zones.isEmpty()) {
            item {
                val shape = RoundedCornerShape(16.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.surface, shape)
                        .border(1.dp, SurfaceVar, shape)
                        .padding(20.dp)
                ) {
                    Text(
                        stringResource(R.string.pace_no_time),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        } else {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(R.string.pace_training_zones),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        stringResource(R.string.pace_training_zones_sub, selectedGoal.label(), distLabel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
            items(zones) { zone ->
                PaceZoneCard(zone = zone)
            }
        }
    }
}

// ── Components ────────────────────────────────────────────────────────────────

@Composable
private fun GoalChip(goal: GoalType, selected: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (selected) primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                shape
            )
            .border(1.dp, if (selected) primary else SurfaceVar, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            goal.label(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) primary else TextMuted
        )
    }
}

@Composable
private fun TimeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            ),
            placeholder = {
                Text(
                    "00",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    ),
                    color = SurfaceVar,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceVar,
                unfocusedContainerColor = SurfaceVar,
                focusedIndicatorColor = primary,
                unfocusedIndicatorColor = SurfaceVar,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = primary
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}

@Composable
private fun TimeSeparator() {
    Text(
        ":",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Medium,
        color = TextMuted,
        modifier = Modifier.padding(bottom = 28.dp)
    )
}

@Composable
private fun PaceZoneCard(zone: PaceZone) {
    var expanded by rememberSaveable(zone.type) { mutableStateOf(false) }
    val typeColor = workoutTypeColor(zone.type)
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(
                1.dp,
                if (expanded) typeColor.copy(alpha = 0.5f) else SurfaceVar,
                shape
            )
            .clickable { expanded = !expanded }
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Type color bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(typeColor)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(zone.type.typeLabel(), style = MaterialTheme.typography.labelLarge)
                    Text(
                        zone.paceRange,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = typeColor
                    )
                }
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.rotate(if (expanded) 180f else 0f)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    zone.type.zoneDescription(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 34.dp, end = 16.dp, bottom = 16.dp)
                )
            }
        }
    }
}

