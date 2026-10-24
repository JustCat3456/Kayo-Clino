package com.example.clinometer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = GeoPrimaryDark,
  onPrimary = GeoOnPrimaryDark,
  primaryContainer = GeoPrimaryContainerDark,
  onPrimaryContainer = GeoOnPrimaryContainerDark,
  secondary = GeoSecondaryDark,
  onSecondary = GeoOnSecondaryDark,
  secondaryContainer = GeoSecondaryContainerDark,
  onSecondaryContainer = GeoOnSecondaryContainerDark,
  tertiary = GeoTertiaryDark,
  onTertiary = GeoOnTertiaryDark,
  tertiaryContainer = GeoTertiaryContainerDark,
  onTertiaryContainer = GeoOnTertiaryContainerDark,
  background = GeoBackgroundDark,
  surface = GeoSurfaceDark,
  surfaceVariant = GeoSurfaceVariantDark,
  onBackground = GeoOnSurfaceDark,
  onSurface = GeoOnSurfaceDark,
  onSurfaceVariant = GeoOnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
  primary = GeoPrimaryLight,
  onPrimary = GeoOnPrimaryLight,
  primaryContainer = GeoPrimaryContainerLight,
  onPrimaryContainer = GeoOnPrimaryContainerLight,
  secondary = GeoSecondaryLight,
  onSecondary = GeoOnSecondaryLight,
  secondaryContainer = GeoSecondaryContainerLight,
  onSecondaryContainer = GeoOnSecondaryContainerLight,
  tertiary = GeoTertiaryLight,
  onTertiary = GeoOnTertiaryLight,
  tertiaryContainer = GeoTertiaryContainerLight,
  onTertiaryContainer = GeoOnTertiaryContainerLight,
  background = GeoBackgroundLight,
  surface = GeoSurfaceLight,
  surfaceVariant = GeoSurfaceVariantLight,
  onBackground = GeoOnSurfaceLight,
  onSurface = GeoOnSurfaceLight,
  onSurfaceVariant = GeoOnSurfaceVariantLight
)

@Composable
fun ClinometerTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // フィールドワーク用の高コントラスト配色をデフォルト優先
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }

    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    content = content
  )
}
