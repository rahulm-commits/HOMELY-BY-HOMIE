package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
  primary = DarkSaffronPrimary,
  onPrimary = SaffronOnContainer,
  primaryContainer = DarkSaffronContainer,
  onPrimaryContainer = SaffronContainer,
  secondary = DarkForestSecondary,
  onSecondary = ForestOnContainer,
  secondaryContainer = DarkForestContainer,
  onSecondaryContainer = ForestContainer,
  tertiary = TurmericContainer,
  onTertiary = TurmericOnContainer,
  background = DarkBackground,
  onBackground = Color(0xFFECEFE9),
  surface = DarkSurface,
  onSurface = Color(0xFFECEFE9),
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = Color(0xFFB5BDB7),
  outline = DarkOutline,
  error = Color(0xFFFF8A80),
  onError = Color(0xFF5D0F0A),
)

private val LightColorScheme = lightColorScheme(
  primary = SaffronPrimary,
  onPrimary = SaffronOnPrimary,
  primaryContainer = SaffronContainer,
  onPrimaryContainer = SaffronOnContainer,
  secondary = ForestSecondary,
  onSecondary = ForestOnSecondary,
  secondaryContainer = ForestContainer,
  onSecondaryContainer = ForestOnContainer,
  tertiary = TurmericTertiary,
  onTertiary = TurmericOnTertiary,
  tertiaryContainer = TurmericContainer,
  onTertiaryContainer = TurmericOnContainer,
  background = WarmIvoryBackground,
  onBackground = DeepSlateInk,
  surface = WarmIvorySurface,
  onSurface = DeepSlateInk,
  surfaceVariant = WarmIvorySurfaceVariant,
  onSurfaceVariant = MutedSlateInk,
  outline = WarmIvoryOutline,
  error = CoralDanger,
  onError = Color.White,
  errorContainer = CoralDangerContainer,
  onErrorContainer = Color(0xFF6B120C),
)

val AppShapes = Shapes(
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(22.dp),
)

@Composable
fun SnackRouteTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = AppShapes,
    content = content,
  )
}
