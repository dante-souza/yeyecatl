package io.github.dante_souza.yeyecatl.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF006B60),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF8EF7E6),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF00658A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC4E7FF),
    onSecondaryContainer = Color(0xFF001E2C),
    background = Color(0xFFF6FAFC),
    onBackground = Color(0xFF171C1F),
    surface = Color(0xFFF6FAFC),
    onSurface = Color(0xFF171C1F),
    surfaceVariant = Color(0xFFDCE5E8),
    onSurfaceVariant = Color(0xFF40484B),
    outline = Color(0xFF70797C)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF40D9C5),
    onPrimary = Color(0xFF003730),
    primaryContainer = Color(0xFF005047),
    onPrimaryContainer = Color(0xFF8EF7E6),
    secondary = Color(0xFF7DD1FF),
    onSecondary = Color(0xFF00344A),
    secondaryContainer = Color(0xFF004C69),
    onSecondaryContainer = Color(0xFFC4E7FF),
    background = Color(0xFF071923),
    onBackground = Color(0xFFDCE4E8),
    surface = Color(0xFF0C202A),
    onSurface = Color(0xFFDCE4E8),
    surfaceVariant = Color(0xFF31474F),
    onSurfaceVariant = Color(0xFFBFC8CC),
    outline = Color(0xFF899297)
)

private val YeyecatlTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

@Composable
fun YeyecatlTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = YeyecatlTypography,
        content = content
    )
}
