package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val YamahaColorScheme = darkColorScheme(
    primary = YamahaRedAccent,
    onPrimary = Color.White,
    primaryContainer = YamahaRedDark,
    onPrimaryContainer = Color(0xFFFFDADA),
    secondary = YamahaCyanLcd,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF9EEFFF),
    tertiary = YamahaGold,
    onTertiary = Color(0xFF432C00),
    background = YamahaDarkChassis,
    onBackground = Color(0xFFE2E4EC),
    surface = YamahaPanelSurface,
    onSurface = Color(0xFFE2E4EC),
    surfaceVariant = YamahaPanelCard,
    onSurfaceVariant = Color(0xFFC4C8D4),
    outline = YamahaBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = YamahaColorScheme,
        typography = Typography,
        content = content
    )
}
