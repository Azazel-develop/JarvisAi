package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JarvisGalaxyColorScheme = darkColorScheme(
  primary = VioletNeon,
  onPrimary = GalaxyVoid,
  primaryContainer = VoidSurfaceVariant,
  onPrimaryContainer = TextGlow,
  secondary = CosmicCyan,
  onSecondary = GalaxyVoid,
  secondaryContainer = VoidSurface,
  onSecondaryContainer = CosmicCyan,
  tertiary = ArcReactorBlue,
  onTertiary = GalaxyVoid,
  background = GalaxyVoid,
  onBackground = TextGlow,
  surface = GalaxyDarkPurple,
  onSurface = TextGlow,
  surfaceVariant = VoidSurface,
  onSurfaceVariant = TextMuted,
  outline = VoidBorder
)

@Composable
fun JarvisTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = JarvisGalaxyColorScheme,
    typography = Typography,
    content = content
  )
}

