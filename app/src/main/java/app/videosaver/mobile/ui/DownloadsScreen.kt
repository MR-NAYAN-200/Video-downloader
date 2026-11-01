package app.videosaver.mobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.videosaver.mobile.DownloadItem
import app.videosaver.mobile.DownloadState
import app.videosaver.mobile.VideoDownloader
import java.util.Locale

@Composable
fun DownloadsScreen(
    items: List<DownloadItem>,
    onOpenDownload: (DownloadItem) -> Unit,
) {
    val activeCount = items.count(DownloadItem::isActive)
    val completedCount = items.count { it.state == DownloadState.COMPLETED }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        item {
            Column {
                Text(
                    "Downloads",
                    color = AppColors.text,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    "ভিডিওগুলো Downloads/Nayan ফোল্ডারে থাকবে।",
                    color = AppColors.muted,
                    fontSize = 12.sp,
                )
                Spacer(Modifier.height(15.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    SummaryPill(
                        label = "$activeCount চলছে",
                        color = AppColors.cyan,
                    )
                    SummaryPill(
                        label = "$completedCount সম্পন্ন",
                        color = AppColors.mint,
                    )
                }
            }
        }

        if (items.isEmpty()) {
            item { EmptyDownloadsCard() }
        } else {
            items(items, key = DownloadItem::id) { item ->
                DownloadCard(
                    item = item,
                    onOpen = { onOpenDownload(item) },
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun SummaryPill(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = CircleShape,
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun EmptyDownloadsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.panel),
        border = BorderStroke(1.dp, AppColors.border),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 35.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                color = AppColors.panelRaised,
                shape = CircleShape,
                modifier = Modifier.size(68.dp),
            ) {
                Icon(
                    Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    tint = AppColors.cyan,
                    modifier = Modifier.padding(19.dp),
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "এখনো কোনো download নেই",
                color = AppColors.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Home থেকে ভিডিও খুঁজে Download করলে progress ও file এখানে দেখাবে।",
                color = AppColors.muted,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun DownloadCard(
    item: DownloadItem,
    onOpen: () -> Unit,
) {
    val stateColor = when (item.state) {
        DownloadState.PENDING -> AppColors.amber
        DownloadState.DOWNLOADING -> AppColors.cyan
        DownloadState.PAUSED -> AppColors.amber
        DownloadState.COMPLETED -> AppColors.mint
        DownloadState.FAILED -> AppColors.red
    }
    val stateLabel = when (item.state) {
        DownloadState.PENDING -> "Waiting"
        DownloadState.DOWNLOADING -> "Downloading"
        DownloadState.PAUSED -> "Paused"
        DownloadState.COMPLETED -> "Completed"
        DownloadState.FAILED -> "Failed"
    }
    val stateIcon = when (item.state) {
        DownloadState.PENDING,
        DownloadState.DOWNLOADING -> Icons.Outlined.Download
        DownloadState.PAUSED -> Icons.Outlined.Pause
        DownloadState.COMPLETED -> Icons.Outlined.CheckCircle
        DownloadState.FAILED -> Icons.Outlined.ErrorOutline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.panel),
        border = BorderStroke(1.dp, AppColors.border),
    ) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    color = stateColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(13.dp),
                ) {
                    Icon(
                        stateIcon,
                        contentDescription = null,
                        tint = stateColor,
                        modifier = Modifier.padding(10.dp),
                    )
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item.title.ifBlank { "ভিডিও" },
                        color = AppColors.text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "$stateLabel  ·  ${formatBytes(item.downloadedBytes)}" +
                            if (item.totalBytes > 0L) " / ${formatBytes(item.totalBytes)}" else "",
                        color = stateColor,
                        fontSize = 11.sp,
                    )
                }
            }

            if (item.isActive) {
                Spacer(Modifier.height(14.dp))
                val progress = item.progress
                if (progress == null) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColors.cyan,
                        trackColor = AppColors.panelRaised,
                    )
                } else {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColors.cyan,
                        trackColor = AppColors.panelRaised,
                    )
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    progress?.let { "${(it * 100).toInt()}% complete" } ?: "Preparing download…",
                    color = AppColors.muted,
                    fontSize = 11.sp,
                )
            } else if (item.state == DownloadState.COMPLETED) {
                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick = onOpen,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Icon(
                        Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        tint = AppColors.mint,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("ভিডিও চালান", color = AppColors.mint, fontWeight = FontWeight.SemiBold)
                }
            } else if (item.state == DownloadState.FAILED) {
                Spacer(Modifier.height(8.dp))
                Text(
                    VideoDownloader.failureMessage(item.reason),
                    color = AppColors.red,
                    fontSize = 11.sp,
                )
            } else {
                Spacer(Modifier.height(8.dp))
                Text("DownloadManager progress notification-এ দেখুন", color = AppColors.muted, fontSize = 11.sp)
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1_000L) return "$bytes B"
    val kilobytes = bytes / 1_000.0
    if (kilobytes < 1_000.0) return String.format(Locale.getDefault(), "%.0f KB", kilobytes)

    val megabytes = kilobytes / 1_000.0
    if (megabytes < 1_000.0) return String.format(Locale.getDefault(), "%.1f MB", megabytes)
    return String.format(Locale.getDefault(), "%.2f GB", megabytes / 1_000.0)
}
