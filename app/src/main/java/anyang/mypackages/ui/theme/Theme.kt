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
    primary = SystemBlue,
    onPrimary = Color.White,
    primaryContainer = BlueLight,
    onPrimaryContainer = SystemBlue,
    secondary = SystemGreen,
    onSecondary = Color.White,
    secondaryContainer = GreenLight,
    onSecondaryContainer = SystemGreen,
    tertiary = SystemOrange,
    onTertiary = Color.White,
    tertiaryContainer = OrangeLight,
    onTertiaryContainer = SystemOrange,
    error = SystemRed,
    onError = Color.White,
    errorContainer = RedLight,
    onErrorContainer = SystemRed,
    background = SystemGroupedBackground,
    onBackground = Label,
    surface = SystemBackground,
    onSurface = Label,
    surfaceVariant = SystemGroupedBackground,
    onSurfaceVariant = SecondaryLabel,
    outline = OpaqueSeparator,
    outlineVariant = Separator
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF0A84FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0040DD),
    onPrimaryContainer = Color(0xFFB3D7FF),
    secondary = Color(0xFF30D158),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF007A3D),
    onSecondaryContainer = Color(0xFFA3EBB5),
    tertiary = Color(0xFFFF9F0A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC44E00),
    onTertiaryContainer = Color(0xFFFFD7A0),
    error = Color(0xFFFF453A),
    onError = Color.White,
    errorContainer = Color(0xFF930015),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF5F5F7),
    surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFF5F5F7),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFAEAEB2),
    outline = Color(0xFF545458),
    outlineVariant = Color(0xFF38383A)
)

@Composable
fun MyPackagesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

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
