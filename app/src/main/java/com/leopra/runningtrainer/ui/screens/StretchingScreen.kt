package com.leopra.runningtrainer.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leopra.runningtrainer.R
import com.leopra.runningtrainer.domain.model.StretchExercise
import com.leopra.runningtrainer.domain.model.postRunRoutine
import com.leopra.runningtrainer.domain.model.preRunRoutine
import com.leopra.runningtrainer.ui.theme.SurfaceVar
import com.leopra.runningtrainer.ui.theme.TextMuted

@Composable
fun StretchingScreen(
    innerPadding: PaddingValues,
    isPreRun: Boolean,
    onBack: () -> Unit
) {
    val exercises = if (isPreRun) preRunRoutine else postRunRoutine
    val accentColor = if (isPreRun) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val bannerTitle = if (isPreRun) stringResource(R.string.stretch_dynamic_heading) else stringResource(R.string.stretch_static_heading)
    val bannerBody = if (isPreRun) stringResource(R.string.stretch_pre_run_banner) else stringResource(R.string.stretch_post_run_banner)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Banner
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(bannerTitle, style = MaterialTheme.typography.labelLarge, color = accentColor)
                Spacer(Modifier.height(4.dp))
                Text(bannerBody, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        }

        item {
            Text(
                stringResource(R.string.stretch_tip),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        items(exercises) { exercise ->
            StretchExerciseCard(exercise = exercise, accentColor = accentColor)
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StretchExerciseCard(exercise: StretchExercise, accentColor: Color) {
    var expanded by rememberSaveable(exercise.id) { mutableStateOf(false) }
    val context = LocalContext.current
    val borderColor = if (expanded) accentColor.copy(alpha = 0.4f) else SurfaceVar

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                Text(exercise.durationLabel, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        // Muscle chips
        Spacer(Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            exercise.muscleGroups.forEach { muscle ->
                Text(
                    muscle,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = accentColor
                )
            }
        }

        // Expanded content
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = SurfaceVar)
                Spacer(Modifier.height(14.dp))
                Text(exercise.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        val query = Uri.encode(exercise.youtubeQuery)
                        val uri = Uri.parse("https://www.youtube.com/results?search_query=$query")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.stretch_watch_tutorial))
                }
            }
        }
    }
}
