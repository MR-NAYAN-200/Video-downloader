package app.videosaver.mobile.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.videosaver.mobile.BuildConfig
import app.videosaver.mobile.R

private const val CREATOR_FACEBOOK =
    "https://www.facebook.com/profile.php?id=100000959749712"
private const val CREATOR_WHATSAPP = "https://wa.me/8801615298449"

@Composable
fun InfoScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column {
            Text(
                "Info",
                color = AppColors.text,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(5.dp))
            Text("অ্যাপ, device এবং creator-এর তথ্য", color = AppColors.muted, fontSize = 12.sp)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.panel),
            border = BorderStroke(1.dp, AppColors.border),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(AppColors.panelRaised, AppColors.panel),
                        ),
                    )
                    .padding(17.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.ic_downloader_mark),
                    contentDescription = "Video Downloader app icon",
                    modifier = Modifier.size(62.dp),
                )
                Spacer(Modifier.width(13.dp))
                Column {
                    Text(
                        "Video Downloader",
                        color = AppColors.text,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text("Fast • Simple • Organized", color = AppColors.muted, fontSize = 11.sp)
                    Spacer(Modifier.height(7.dp))
                    InfoTag("Version ${BuildConfig.VERSION_NAME}")
                }
            }
        }

        InfoSectionCard(
            title = "App info",
            icon = Icons.Outlined.Apps,
        ) {
            InfoRow("App name", "Video Downloader")
            InfoRow("Version", BuildConfig.VERSION_NAME)
            InfoRow("Download folder", "Downloads/Nayan")
            InfoRow("Built with", "Kotlin • Jetpack Compose")
            InfoRow("Video service", "Nayan Downloader API")
        }

        InfoSectionCard(
            title = "Device info",
            icon = Icons.Outlined.Smartphone,
        ) {
            InfoRow("Device", "${Build.MANUFACTURER} ${Build.MODEL}".trim())
            InfoRow("Android", "Android ${Build.VERSION.RELEASE}")
            InfoRow("API level", Build.VERSION.SDK_INT.toString())
            InfoRow("Device code", Build.DEVICE)
        }

        InfoSectionCard(
            title = "Creator info",
            icon = Icons.Outlined.Person,
        ) {
            Text(
                "Mohammad Nayan",
                color = AppColors.text,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Downloader API developer",
                color = AppColors.muted,
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(13.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedButton(
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(CREATOR_FACEBOOK))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    },
                    shape = RoundedCornerShape(13.dp),
                    border = BorderStroke(1.dp, AppColors.border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.cyan),
                ) {
                    Icon(Icons.Outlined.Language, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Facebook")
                }
                OutlinedButton(
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(CREATOR_WHATSAPP))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    },
                    shape = RoundedCornerShape(13.dp),
                    border = BorderStroke(1.dp, AppColors.border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.mint),
                ) {
                    Icon(Icons.Outlined.Chat, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("WhatsApp")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.panel),
            border = BorderStroke(1.dp, AppColors.border),
        ) {
            Row(
                modifier = Modifier.padding(15.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = AppColors.amber,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    "শুধু নিজের তৈরি বা ডাউনলোডের অনুমতি থাকা ভিডিও সংরক্ষণ করুন।",
                    color = AppColors.muted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun InfoSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.panel),
        border = BorderStroke(1.dp, AppColors.border),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = AppColors.cyan, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, color = AppColors.text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(13.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, color = AppColors.muted, fontSize = 12.sp, modifier = Modifier.weight(0.9f))
        Text(
            value,
            color = AppColors.text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1.1f),
        )
    }
}

@Composable
private fun InfoTag(text: String) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(AppColors.mint.copy(alpha = 0.13f))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.Apps,
            contentDescription = null,
            tint = AppColors.mint,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(5.dp))
        Text(text, color = AppColors.mint, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}
