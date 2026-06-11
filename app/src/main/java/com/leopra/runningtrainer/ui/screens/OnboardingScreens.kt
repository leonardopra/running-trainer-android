package com.leopra.runningtrainer.ui.screens

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.leopra.runningtrainer.R
import com.leopra.runningtrainer.domain.model.FitnessLevel
import com.leopra.runningtrainer.domain.model.GoalType
import com.leopra.runningtrainer.ui.MainUiState
import com.leopra.runningtrainer.ui.theme.SurfaceVar
import com.leopra.runningtrainer.ui.theme.TextMuted
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

// ── Onboarding progress indicator ────────────────────────────────────────────

@Composable
private fun OnboardingProgress(step: Int, total: Int = 5) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(total) { i ->
            val primary = MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i < step) primary else SurfaceVar)
            )
        }
    }
}

// ── Continue button ───────────────────────────────────────────────────────────

@Composable
private fun ContinueButton(
    text: String = "Continue",
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.background,
            disabledContainerColor = SurfaceVar,
            disabledContentColor = TextMuted
        )
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

// ── Filled text field (Flutter style) ────────────────────────────────────────

@Composable
private fun FilledField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val primary = MaterialTheme.colorScheme.primary
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
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

// ── Race date field with calendar picker ─────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RaceDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    val primary = MaterialTheme.colorScheme.primary
    val focusManager = LocalFocusManager.current
    var showPicker by remember { mutableStateOf(false) }

    val initialMillis = remember(value) {
        value.takeIf { it.isNotBlank() }
            ?.let { runCatching { LocalDate.parse(it).toEpochDay() * 86_400_000L }.getOrNull() }
    }
    val today = LocalDate.now().toEpochDay() * 86_400_000L

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, color = TextMuted) },
        placeholder = { Text("YYYY-MM-DD", color = TextMuted) },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = primary
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
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

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis ?: today,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= today
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                            .toString()
                        onValueChange(date)
                    }
                    showPicker = false
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

// ── Screens ───────────────────────────────────────────────────────────────────

@Composable
fun GoalSelectionScreen(
    innerPadding: PaddingValues,
    onGoalSelected: (GoalType) -> Unit
) {
    val sFiveK   = stringResource(R.string.goal_five_k)
    val sTenK    = stringResource(R.string.goal_ten_k)
    val sHalf    = stringResource(R.string.goal_half_marathon)
    val sMarath  = stringResource(R.string.goal_marathon)
    val sTrail   = stringResource(R.string.goal_trail_run)
    val sFitness = stringResource(R.string.goal_general_fitness)
    SelectionListScreen(
        innerPadding = innerPadding,
        step = 1,
        title = stringResource(R.string.onboarding_goal_title),
        subtitle = stringResource(R.string.onboarding_goal_subtitle),
        items = GoalType.entries,
        itemEmoji = {
            when (it) {
                GoalType.fiveK          -> "🏃"
                GoalType.tenK           -> "🏅"
                GoalType.halfMarathon   -> "🌟"
                GoalType.marathon       -> "🏆"
                GoalType.trailRun       -> "🏔️"
                GoalType.generalFitness -> "💪"
            }
        },
        itemTitle = {
            when (it) {
                GoalType.fiveK          -> sFiveK
                GoalType.tenK           -> sTenK
                GoalType.halfMarathon   -> sHalf
                GoalType.marathon       -> sMarath
                GoalType.trailRun       -> sTrail
                GoalType.generalFitness -> sFitness
            }
        },
        onSelected = onGoalSelected
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RaceConfigScreen(
    innerPadding: PaddingValues,
    uiState: MainUiState,
    onConfigChanged: (String, Int?) -> Unit,
    onContinue: () -> Unit
) {
    val form = uiState.onboarding
    val goal = form.goalType
    val suggestedDuration = when (goal) {
        GoalType.fiveK          -> 8
        GoalType.tenK           -> 10
        GoalType.halfMarathon   -> 12
        GoalType.marathon       -> 16
        GoalType.trailRun       -> 14
        GoalType.generalFitness, null -> 8
    }
    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        OnboardingProgress(step = 2)
        Text(stringResource(R.string.onboarding_race_setup_title), style = MaterialTheme.typography.displayLarge)
        Text(
            stringResource(R.string.onboarding_race_setup_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        RaceDateField(
            value = form.raceDateInput,
            onValueChange = { onConfigChanged(it, if (it.isNotBlank()) null else form.durationWeeks) },
            label = stringResource(R.string.onboarding_race_date_label)
        )

        Text(stringResource(R.string.onboarding_or_choose_duration), style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(suggestedDuration, 8, 10, 12, 14, 16).distinct().forEach { weeks ->
                val selected = form.durationWeeks == weeks && form.raceDateInput.isBlank()
                DurationChip(
                    label = stringResource(R.string.onboarding_weeks_chip, weeks),
                    selected = selected,
                    onClick = { onConfigChanged("", weeks) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        ContinueButton(text = stringResource(R.string.btn_continue), onClick = {
            if (form.raceDateInput.isBlank() && form.durationWeeks == null) {
                onConfigChanged("", suggestedDuration)
            }
            onContinue()
        })
    }
}

@Composable
fun FitnessSelectionScreen(
    innerPadding: PaddingValues,
    onFitnessSelected: (FitnessLevel) -> Unit
) {
    val sBeginner         = stringResource(R.string.fitness_beginner)
    val sIntermediate     = stringResource(R.string.fitness_intermediate)
    val sAdvanced         = stringResource(R.string.fitness_advanced)
    val sBeginnerDesc     = stringResource(R.string.fitness_beginner_desc)
    val sIntermediateDesc = stringResource(R.string.fitness_intermediate_desc)
    val sAdvancedDesc     = stringResource(R.string.fitness_advanced_desc)
    SelectionListScreen(
        innerPadding = innerPadding,
        step = 3,
        title = stringResource(R.string.onboarding_fitness_title),
        subtitle = stringResource(R.string.onboarding_fitness_subtitle),
        items = FitnessLevel.entries,
        itemEmoji = {
            when (it) {
                FitnessLevel.beginner     -> "🌱"
                FitnessLevel.intermediate -> "⚡"
                FitnessLevel.advanced     -> "🔥"
            }
        },
        itemTitle = {
            when (it) {
                FitnessLevel.beginner     -> sBeginner
                FitnessLevel.intermediate -> sIntermediate
                FitnessLevel.advanced     -> sAdvanced
            }
        },
        itemBody = {
            when (it) {
                FitnessLevel.beginner     -> sBeginnerDesc
                FitnessLevel.intermediate -> sIntermediateDesc
                FitnessLevel.advanced     -> sAdvancedDesc
            }
        },
        onSelected = onFitnessSelected
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrainingDaysScreen(
    innerPadding: PaddingValues,
    selectedDays: Int,
    onDaysChanged: (Int) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        OnboardingProgress(step = 4)
        Text(stringResource(R.string.onboarding_days_title), style = MaterialTheme.typography.displayLarge)
        Text(
            stringResource(R.string.onboarding_days_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            (3..6).forEach { dayCount ->
                DurationChip(
                    label = stringResource(R.string.onboarding_days_chip, dayCount),
                    selected = selectedDays == dayCount,
                    onClick = { onDaysChanged(dayCount) }
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        ContinueButton(text = stringResource(R.string.btn_continue), onClick = onContinue)
    }
}

@Composable
fun ProfileScreen(
    innerPadding: PaddingValues,
    uiState: MainUiState,
    onProfileChanged: (String, String, String, String) -> Unit,
    onGeneratePlan: () -> Unit
) {
    val form = uiState.onboarding
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OnboardingProgress(step = 5)
        Text(stringResource(R.string.onboarding_profile_title), style = MaterialTheme.typography.displayLarge)
        Text(
            stringResource(R.string.onboarding_profile_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FilledField(
            value = form.name,
            onValueChange = { onProfileChanged(it, form.age, form.weightKg, form.heightCm) },
            label = stringResource(R.string.field_name)
        )
        FilledField(
            value = form.age,
            onValueChange = { onProfileChanged(form.name, it, form.weightKg, form.heightCm) },
            label = stringResource(R.string.field_age),
            keyboardType = KeyboardType.Number
        )
        FilledField(
            value = form.weightKg,
            onValueChange = { onProfileChanged(form.name, form.age, it, form.heightCm) },
            label = stringResource(R.string.field_weight_kg),
            keyboardType = KeyboardType.Decimal
        )
        FilledField(
            value = form.heightCm,
            onValueChange = { onProfileChanged(form.name, form.age, form.weightKg, it) },
            label = stringResource(R.string.field_height_cm),
            keyboardType = KeyboardType.Decimal
        )

        uiState.generationError?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.weight(1f))
        ContinueButton(
            text = if (uiState.isGeneratingPlan) stringResource(R.string.btn_generating)
                   else stringResource(R.string.btn_generate_plan),
            enabled = !uiState.isGeneratingPlan && form.goalType != null && form.fitnessLevel != null,
            onClick = onGeneratePlan
        )
    }
}

@Composable
fun GeneratingPlanScreen(innerPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.generating_title), style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.generating_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Reusable selection card list ──────────────────────────────────────────────

@Composable
private fun <T> SelectionListScreen(
    innerPadding: PaddingValues,
    step: Int,
    title: String,
    subtitle: String,
    items: List<T>,
    itemTitle: (T) -> String,
    itemEmoji: ((T) -> String)? = null,
    itemBody: ((T) -> String)? = null,
    onSelected: (T) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OnboardingProgress(step = step)
                Text(title, style = MaterialTheme.typography.displayLarge)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(items) { item ->
            SelectionCard(
                title = itemTitle(item),
                emoji = itemEmoji?.invoke(item),
                body = itemBody?.invoke(item),
                onClick = { onSelected(item) }
            )
        }
    }
}

@Composable
private fun SelectionCard(
    title: String,
    emoji: String? = null,
    body: String? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, SurfaceVar, shape)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (emoji != null) {
                Text(emoji, style = MaterialTheme.typography.titleLarge)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                if (body != null) {
                    Text(body, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                }
            }
        }
    }
}

@Composable
private fun DurationChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(20.dp)
    val bg = if (selected) primary.copy(alpha = 0.15f) else SurfaceVar.copy(alpha = 0.5f)
    val border = if (selected) primary else SurfaceVar
    val textColor = if (selected) primary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .clip(shape)
            .background(bg, shape)
            .border(1.dp, border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = textColor)
    }
}
