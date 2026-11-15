package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.core.view.WindowCompat

private val DarkColorScheme =
  darkColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color(0xFF0A0E17),
    primaryContainer = IndigoContainerDark,
    onPrimaryContainer = Color(0xFFE0E5FF),
    secondary = SkySecondary,
    onSecondary = Color(0xFF062030),
    secondaryContainer = SkyContainerDark,
    onSecondaryContainer = Color(0xFFD6F2FF),
    tertiary = AmberTertiary,
    onTertiary = Color(0xFF2E1E00),
    tertiaryContainer = AmberContainerDark,
    onTertiaryContainer = Color(0xFFFEF0C7),
    background = MidnightCanvas,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurfaceElevated,
    surfaceContainerHigh = DarkSurfaceHighest,
    outline = DarkBorder,
    outlineVariant = Color(0xFF1F293D),
    error = RoseIncorrect,
    onError = Color.White,
    errorContainer = RoseIncorrectContainerDark,
    onErrorContainer = Color(0xFFFFD9DF),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = IndigoPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = IndigoContainerLight,
    onPrimaryContainer = Color(0xFF1A1E5A),
    secondary = SkySecondaryDark,
    onSecondary = Color.White,
    secondaryContainer = SkyContainerLight,
    onSecondaryContainer = Color(0xFF04304B),
    tertiary = AmberTertiaryDark,
    onTertiary = Color.White,
    tertiaryContainer = AmberContainerLight,
    onTertiaryContainer = Color(0xFF452800),
    background = LightCanvas,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightSurfaceElevated,
    surfaceContainerHigh = LightSurfaceHighest,
    outline = LightBorder,
    outlineVariant = Color(0xFFE5EAF4),
    error = RoseIncorrect,
    onError = Color.White,
    errorContainer = RoseIncorrectContainerLight,
    onErrorContainer = Color(0xFF600B1B),
  )

val AppShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

@Composable
fun CmaPrepTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        WindowCompat.getInsetsController(window, view).apply {
          isAppearanceLightStatusBars = !darkTheme
          isAppearanceLightNavigationBars = !darkTheme
        }
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = AppShapes,
    content = content,
  )
}
