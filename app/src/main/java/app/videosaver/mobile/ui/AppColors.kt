package app.videosaver.mobile.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object AppColors {
    val background = Color(0xFF07111E)
    val backgroundTop = Color(0xFF0C2035)
    val panel = Color(0xFF101F30)
    val panelRaised = Color(0xFF172B3E)
    val border = Color(0xFF294258)
    val cyan = Color(0xFF56D8FF)
    val mint = Color(0xFF8BF0D0)
    val text = Color(0xFFF2F7FC)
    val muted = Color(0xFF9AAFC1)
    val green = Color(0xFF8BE3B0)
    val amber = Color(0xFFFFCF7A)
    val red = Color(0xFFFF9299)
    val darkInk = Color(0xFF071629)

    val backgroundBrush: Brush
        get() = Brush.verticalGradient(
            colors = listOf(backgroundTop, background, background),
        )
}

@Composable
fun VideoDownloaderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = AppColors.mint,
            onPrimary = AppColors.darkInk,
            secondary = AppColors.cyan,
            onSecondary = AppColors.darkInk,
            background = AppColors.background,
            surface = AppColors.panel,
            surfaceVariant = AppColors.panelRaised,
            onBackground = AppColors.text,
            onSurface = AppColors.text,
            onSurfaceVariant = AppColors.muted,
            error = AppColors.red,
        ),
        content = content,
    )
}
