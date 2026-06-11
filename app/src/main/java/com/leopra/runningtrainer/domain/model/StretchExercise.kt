package com.leopra.runningtrainer.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class StretchExercise(
    val id: String,
    val name: String,
    val description: String,
    val muscleGroups: List<String>,
    val durationSeconds: Int,
    val reps: String?,
    val isPreRun: Boolean,
    val youtubeQuery: String
) {
    val durationLabel: String get() = if (reps != null) reps else "${durationSeconds}s each side"
}
