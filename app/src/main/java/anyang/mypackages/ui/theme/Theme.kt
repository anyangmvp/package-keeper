package anyang.mypackages.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ProfessionalBlue,
    secondary = ModernTeal,
    tertiary = VibrantOrange,
    background = CoolWhite,
    surface = CardBackground,
    surfaceVariant = SoftGray,
    onPrimary = TextInverse,
    onSecondary = TextInverse,
    onTertiary = TextInverse,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = TextInverse,
    outline = CardBorder,
    primaryContainer = LightBlue,
    onPrimaryContainer = DeepBlue,
    secondaryContainer = TealLight,
    onSecondaryContainer = ModernTeal,
    tertiaryContainer = OrangeLight,
    onTertiaryContainer = VibrantOrange
)

private val DarkColorScheme = darkColorScheme(
    primary = LightBlue,
    secondary = TealLight,
    tertiary = WarmOrange,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155),
    onPrimary = DeepBlue,
    onSecondary = ModernTeal,
    onTertiary = Color(0xFF7C2D12),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF7F1D1D),
    outline = Color(0xFF475569),
    primaryContainer = DeepBlue,
    onPrimaryContainer = LightBlue,
    secondaryContainer = ModernTeal,
    onSecondaryContainer = TealLight,
    tertiaryContainer = Color(0xFF7C2D12),
    onTertiaryContainer = OrangeLight
)

@Composable
fun MyPackagesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
