package com.leopra.runningtrainer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RunningTrainerColors = darkColorScheme(
    primary                = Primary,
    primaryContainer       = Color(0xFF003640),
    onPrimaryContainer     = Primary,
    secondary              = Secondary,
    secondaryContainer     = Color(0xFF1A3300),
    onSecondaryContainer   = Secondary,
    tertiary               = Tertiary,
    tertiaryContainer      = TertiaryContainer,
    background             = Background,
    surface                = Surface,
    surfaceVariant         = SurfaceVar,
    error                  = ErrorRed,
    onPrimary              = Background,
    onSecondary            = Background,
    onTertiary             = Background,
    onBackground           = OnDark,
    onSurface              = OnDark,
    onSurfaceVariant       = TextMuted,
    onError                = OnDark
)

@Composable
fun RunningTrainerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RunningTrainerColors,
        typography  = Typography,
        content     = content
    )
}
