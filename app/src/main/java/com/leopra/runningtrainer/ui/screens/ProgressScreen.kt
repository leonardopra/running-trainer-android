package com.leopra.runningtrainer.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import com.leopra.runningtrainer.domain.model.ProgressStats
import com.leopra.runningtrainer.domain.model.WorkoutFeeling
import com.leopra.runningtrainer.domain.model.WorkoutType
import androidx.compose.ui.res.stringResource
import com.leopra.runningtrainer.R
import com.leopra.runningtrainer.ui.theme.ColorEasyRun
import com.leopra.runningtrainer.ui.theme.ColorFeelingGreat
import com.leopra.runningtrainer.ui.theme.ColorFeelingGood
import com.leopra.runningtrainer.ui.theme.ColorFeelingInjured
import com.leopra.runningtrainer.ui.theme.ColorFeelingOk
import com.leopra.runningtrainer.ui.theme.ColorFeelingTired
import com.leopra.runningtrainer.ui.theme.ColorIntervalRun
import com.leopra.runningtrainer.ui.theme.Secondary
import com.leopra.runningtrainer.ui.theme.SurfaceVar
import com.leopra.runningtrainer.ui.theme.TextMuted
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun ProgressScreen(
    innerPadding: PaddingValues,
    progressStats: ProgressStats?,
    onBack: () -> Unit,
    onViewAllHistory: () -> Unit = {}
) {
    if (progressStats == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.no_data_yet), style = MaterialTheme.typography.displayLarge)
            Text(
                stringResource(R.string.progress_no_plan_desc),
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted
            )
        }
        return
    }

    val completionPct = (progressStats.completionRate * 100).toInt()
    val weeksCompleted = progressStats.weeklyProgress.count {
        it.completedWorkouts == it.totalWorkouts && it.totalWorkouts > 0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 2x2 stat grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.progress_completion),
                    value = "$completionPct%",
                    sub = "${progressStats.completedWorkouts}/${progressStats.totalNonRestWorkouts} workouts",
                    accentColor = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.progress_distance),
                    value = "${"%.1f".format(progressStats.totalLoggedKm)} km",
                    sub = stringResource(R.string.progress_of_planned, "%.1f".format(progressStats.totalPlannedKm)),
                    accentColor = ColorIntervalRun
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.progress_streak),
                    value = "${progressStats.currentStreak}",
                    sub = stringResource(R.string.progress_consecutive),
                    accentColor = Secondary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.progress_weeks_done),
                    value = "$weeksCompleted",
                    sub = stringResource(R.string.progress_of_started, progressStats.weeklyProgress.size),
                    accentColor = ColorEasyRun
                )
            }
        }

        // Weekly km chart
        if (progressStats.weeklyKmHistory.isNotEmpty()) {
            item {
                val avgGoalKm = if (progressStats.weeklyProgress.isNotEmpty())
                    progressStats.weeklyProgress.map { it.plannedKm.toFloat() }.average().toFloat()
                    else 0f
                WeeklyKmChart(
                    data = progressStats.weeklyKmHistory,
                    goalKm = avgGoalKm
                )
            }
        }

        // Feeling distribution
        if (progressStats.feelingCounts.isNotEmpty()) {
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.how_youve_felt), style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            WorkoutFeeling.entries.forEach { feeling ->
                                val count = progressStats.feelingCounts[feeling] ?: 0
                                if (count > 0) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(feelingColor(feeling).copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(feelingEmoji(feeling), style = MaterialTheme.typography.titleSmall)
                                        }
                                        Text(
                                            "$count",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Workout type breakdown
        if (progressStats.workoutTypeCounts.isNotEmpty()) {
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(R.string.workout_types), style = MaterialTheme.typography.titleMedium)
                        progressStats.workoutTypeCounts.forEach { typeCount ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(workoutTypeColor(typeCount.type))
                                    )
                                    Text(workoutTypeLabel(typeCount.type), style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(
                                    "${typeCount.count}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Weekly progress
        item {
            Text(stringResource(R.string.weekly_progress), style = MaterialTheme.typography.titleLarge)
        }
        items(progressStats.weeklyProgress) { week ->
            val weekDone = week.completionRate >= 0.8
            val progressColor = if (weekDone) ColorEasyRun else MaterialTheme.colorScheme.primary
            SurfaceCard(
                modifier = Modifier.fillMaxWidth(),
                accentBorder = if (weekDone) ColorEasyRun.copy(alpha = 0.4f) else null
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.home_week_label, week.weekNumber), style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${(week.completionRate * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (weekDone) ColorEasyRun else TextMuted
                        )
                    }
                    LinearProgressIndicator(
                        progress = { week.completionRate.toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = progressColor,
                        trackColor = SurfaceVar
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.week_progress_km, week.loggedKm, week.plannedKm),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            "${week.completedWorkouts}/${week.totalWorkouts} workouts",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Recent activity
        if (progressStats.recentCompletedWorkouts.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.recent_activity), style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = onViewAllHistory) {
                        Text(stringResource(R.string.btn_view_all), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            items(progressStats.recentCompletedWorkouts.take(4)) { workout ->
                val todayStr = stringResource(R.string.today)
                val yesterdayStr = stringResource(R.string.yesterday)
                val relativeDate = workout.completedAt?.let { completedAt ->
                    val completedDate = completedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
                    val completed = LocalDate.of(completedDate.year, completedDate.monthNumber, completedDate.dayOfMonth)
                    val daysAgo = ChronoUnit.DAYS.between(completed, LocalDate.now()).toInt()
                    when (daysAgo) {
                        0    -> todayStr
                        1    -> yesterdayStr
                        else -> stringResource(R.string.days_ago, daysAgo)
                    }
                } ?: ""
                SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(workout.displayTitle(), style = MaterialTheme.typography.bodyLarge)
                            val detail = buildString {
                                workout.actualDistanceKm?.let { append("${"%.1f".format(it)} km") }
                                workout.actualDurationMinutes?.let {
                                    if (isNotEmpty()) append(" • ")
                                    append("$it min")
                                }
                            }
                            if (detail.isNotEmpty()) {
                                Text(detail, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(relativeDate, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            if (workout.rpe != null) {
                                Text(stringResource(R.string.rpe_label, workout.rpe), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                            workout.feeling?.let {
                                Text(feelingEmoji(it), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Shared stat card ──────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    sub: String,
    accentColor: Color = SurfaceVar
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, accentColor.copy(alpha = 0.25f), shape)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun feelingEmoji(feeling: WorkoutFeeling): String = when (feeling) {
    WorkoutFeeling.great   -> "🙌"
    WorkoutFeeling.good    -> "😊"
    WorkoutFeeling.ok      -> "😐"
    WorkoutFeeling.tired   -> "😓"
    WorkoutFeeling.injured -> "🤕"
}

private fun feelingColor(feeling: WorkoutFeeling): Color = when (feeling) {
    WorkoutFeeling.great   -> ColorFeelingGreat
    WorkoutFeeling.good    -> ColorFeelingGood
    WorkoutFeeling.ok      -> ColorFeelingOk
    WorkoutFeeling.tired   -> ColorFeelingTired
    WorkoutFeeling.injured -> ColorFeelingInjured
}

@Composable
private fun workoutTypeLabel(type: WorkoutType): String = when (type) {
    WorkoutType.easyRun     -> stringResource(R.string.workout_type_easy_run)
    WorkoutType.longRun     -> stringResource(R.string.workout_type_long_run)
    WorkoutType.tempoRun    -> stringResource(R.string.workout_type_tempo_run)
    WorkoutType.intervalRun -> stringResource(R.string.workout_type_interval_run)
    WorkoutType.crossTrain  -> stringResource(R.string.workout_type_cross_train)
    WorkoutType.rest        -> stringResource(R.string.workout_type_rest)
}

// ── Weekly km chart ───────────────────────────────────────────────────────────

@Composable
fun WeeklyKmChart(data: List<Pair<String, Float>>, goalKm: Float) {
    if (data.isEmpty()) return

    val primary = MaterialTheme.colorScheme.primary
    val textMutedColor = TextMuted

    // Compute delta vs previous week
    val deltaPct: Float? = if (data.size >= 2) {
        val prev = data[data.size - 2].second
        val curr = data[data.size - 1].second
        if (prev > 0f) (curr - prev) / prev * 100f else null
    } else null
    val deltaText = deltaPct?.let { stringResource(R.string.progress_weekly_km_delta, it) }

    SurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.progress_weekly_km_chart),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
                if (deltaText != null) {
                    Text(
                        deltaText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            val maxKm = maxOf(data.maxOf { it.second }, goalKm, 0.01f)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                val n = data.size
                val totalSlots = n * 1.5f + 0.5f
                val slotWidth = size.width / totalSlots
                val barWidth = slotWidth
                val spacing = slotWidth * 0.5f
                val availableHeight = size.height - 4.dp.toPx()
                val scaleY = availableHeight / maxKm

                data.forEachIndexed { i, (_, km) ->
                    val x = spacing + i * (barWidth + spacing)
                    val barHeight = (km * scaleY).coerceAtLeast(0f)
                    val y = size.height - barHeight
                    val isCurrentWeek = i == n - 1

                    drawRect(
                        color = if (isCurrentWeek) primary.copy(alpha = 0.55f) else primary.copy(alpha = 0.25f),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight)
                    )
                    if (!isCurrentWeek && barHeight > 0f) {
                        drawLine(
                            color = primary.copy(alpha = 0.7f),
                            start = Offset(x, y),
                            end = Offset(x + barWidth, y),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                if (goalKm > 0f) {
                    val goalY = (size.height - goalKm * scaleY).coerceIn(0f, size.height)
                    drawLine(
                        color = textMutedColor.copy(alpha = 0.6f),
                        start = Offset(0f, goalY),
                        end = Offset(size.width, goalY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                data.forEach { (label, _) ->
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
