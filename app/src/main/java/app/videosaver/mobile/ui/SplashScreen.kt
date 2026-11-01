package app.videosaver.mobile.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.videosaver.mobile.R

@Composable
fun LaunchSplashScreen() {
    val pulse = rememberInfiniteTransition(label = "logo pulse")
    val scale by pulse.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "logo scale",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF102F50), AppColors.background, Color(0xFF050B13)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_downloader_mark),
                contentDescription = "Video Downloader logo",
                modifier = Modifier
                    .size(148.dp)
                    .scale(scale),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Video Downloader",
                color = AppColors.text,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "তোমার ভিডিও, এক জায়গায়",
                color = AppColors.muted,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(28.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(25.dp),
                color = AppColors.mint,
                trackColor = AppColors.panelRaised,
                strokeWidth = 2.5.dp,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(6.dp)
                .background(AppColors.cyan, CircleShape),
        )
    }
}
