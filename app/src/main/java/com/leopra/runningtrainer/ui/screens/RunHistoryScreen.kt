package com.leopra.runningtrainer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.leopra.runningtrainer.R
import com.leopra.runningtrainer.domain.model.TrainingPlan
import com.leopra.runningtrainer.domain.model.WorkoutFeeling
import com.leopra.runningtrainer.domain.model.WorkoutType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val FILTER_TYPES = listOf(
    null,
    WorkoutType.easyRun,
    WorkoutType.longRun,
    WorkoutType.tempoRun,
    WorkoutType.intervalRun,
    WorkoutType.crossTrain
)

@Composable
fun RunHistoryScreen(
    innerPadding: PaddingValues,
    activePlan: TrainingPlan?,
    onBack: () -> Unit
) {
    var selectedFilter by rememberSaveable { mutableStateOf<WorkoutType?>(null) }

    val allCompleted = activePlan?.weeks
        ?.flatMap { it.workouts }
        ?.filter { it.isCompleted && it.type != WorkoutType.rest }
        ?.sortedByDescending { it.completedAt }
        ?: emptyList()

    val filtered = if (selectedFilter == null) allCompleted
    else allCompleted.filter { it.type == selectedFilter }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(stringResource(R.string.run_history), style = MaterialTheme.typography.headlineMedium)
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(FILTER_TYPES) { type ->
                    FilterChip(
                        selected = selectedFilter == type,
                        onClick = { selectedFilter = type },
                        label = {
                            Text(
                                when (type) {
                                    null -> "All"
                                    WorkoutType.easyRun -> stringResource(R.string.workout_type_easy_run)
                                    WorkoutType.longRun -> stringResource(R.string.workout_type_long_run)
                                    WorkoutType.tempoRun -> stringResource(R.string.workout_type_tempo_run)
                                    WorkoutType.intervalRun -> stringResource(R.string.workout_type_interval_run)
                                    WorkoutType.crossTrain -> stringResource(R.string.workout_type_cross_train)
                                    else -> type.name
                                }
                            )
                        }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.no_data_yet),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }

        items(filtered) { workout ->
            val today = LocalDate.now()
            val relativeDate = workout.completedAt?.let { at ->
                val d = at.toLocalDateTime(TimeZone.currentSystemDefault()).date
                val completed = LocalDate.of(d.year, d.monthNumber, d.dayOfMonth)
                val daysAgo = ChronoUnit.DAYS.between(completed, today).toInt()
                when (daysAgo) {
                    0 -> stringResource(R.string.today)
                    1 -> stringResource(R.string.yesterday)
                    else -> "$daysAgo days ago"
                }
            } ?: ""

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(workout.title, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            workoutTypeLabelHistory(workout.type),
                            style = MaterialTheme.typography.labelMedium,
                            color = workoutTypeColor(workout.type)
                        )
                        val detail = buildString {
                            workout.actualDistanceKm?.let { append("${"%.1f".format(it)} km") }
                            workout.actualDurationMinutes?.let {
                                if (isNotEmpty()) append(" · ")
                                append("$it min")
                            }
                        }
                        if (detail.isNotEmpty()) {
                            Text(detail, style = MaterialTheme.typography.bodySmall)
                        }
                        if (!workout.notes.isNullOrBlank()) {
                            Text(
                                workout.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(relativeDate, style = MaterialTheme.typography.labelSmall)
                        workout.rpe?.let {
                            Text(stringResource(R.string.rpe_label, it), style = MaterialTheme.typography.labelSmall)
                        }
                        workout.feeling?.let {
                            Text(feelingEmojiHistory(it), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }

        item {
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.btn_back))
            }
        }
    }
}

@Composable
private fun workoutTypeLabelHistory(type: WorkoutType): String = when (type) {
    WorkoutType.easyRun -> stringResource(R.string.workout_type_easy_run)
    WorkoutType.longRun -> stringResource(R.string.workout_type_long_run)
    WorkoutType.tempoRun -> stringResource(R.string.workout_type_tempo_run)
    WorkoutType.intervalRun -> stringResource(R.string.workout_type_interval_run)
    WorkoutType.crossTrain -> stringResource(R.string.workout_type_cross_train)
    WorkoutType.rest -> stringResource(R.string.workout_type_rest)
}


private fun feelingEmojiHistory(feeling: WorkoutFeeling): String = when (feeling) {
    WorkoutFeeling.great -> "🙌"
    WorkoutFeeling.good -> "😊"
    WorkoutFeeling.ok -> "😐"
    WorkoutFeeling.tired -> "😓"
    WorkoutFeeling.injured -> "🤕"
}
