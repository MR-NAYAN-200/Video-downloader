package app.videosaver.mobile.ui

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import app.videosaver.mobile.DownloadQuality
import app.videosaver.mobile.R
import app.videosaver.mobile.VideoApi
import app.videosaver.mobile.VideoDetails
import app.videosaver.mobile.VideoDownloader
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

private data class PendingVideoDownload(
    val title: String,
    val url: String,
)

@Composable
fun HomeScreen(
    activeDownloadCount: Int,
    onOpenDownloads: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var videoUrl by rememberSaveable { mutableStateOf("") }
    var video by remember { mutableStateOf<VideoDetails?>(null) }
    var selectedUrl by remember(video) {
        mutableStateOf(video?.qualities?.lastOrNull()?.url)
    }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var notificationUnavailable by remember { mutableStateOf(false) }
    var pendingDownload by remember { mutableStateOf<PendingVideoDownload?>(null) }

    val enqueueDownload: (PendingVideoDownload) -> Unit = { request ->
        try {
            VideoDownloader.enqueue(context, request.title, request.url)
            errorMessage = null
            statusMessage = if (notificationUnavailable) {
                "ডাউনলোড শুরু হয়েছে। নোটিফিকেশন বন্ধ—Download ট্যাবে progress দেখুন।"
            } else {
                "ডাউনলোড শুরু হয়েছে। notification ও Download ট্যাবে progress দেখতে পারবেন।"
            }
        } catch (exception: Exception) {
            errorMessage = exception.message ?: "ডাউনলোড শুরু করা যায়নি।"
            statusMessage = null
        }
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grantedPermissions ->
        val request = pendingDownload
        pendingDownload = null
        if (request != null) {
            val storageGranted = Build.VERSION.SDK_INT > Build.VERSION_CODES.P ||
                grantedPermissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ) == PackageManager.PERMISSION_GRANTED

            if (!storageGranted) {
                errorMessage = "Downloads ফোল্ডারে সংরক্ষণ করতে storage permission দরকার।"
                statusMessage = null
            } else {
                notificationUnavailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    grantedPermissions[Manifest.permission.POST_NOTIFICATIONS] != true &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) != PackageManager.PERMISSION_GRANTED
                enqueueDownload(request)
            }
        }
    }

    fun startDownload(title: String, url: String) {
        val requestedPermissions = buildList {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        val request = PendingVideoDownload(title = title, url = url)

        if (requestedPermissions.isEmpty()) {
            notificationUnavailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            enqueueDownload(request)
        } else {
            pendingDownload = request
            permissionsLauncher.launch(requestedPermissions.toTypedArray())
        }
    }

    fun searchVideo() {
        scope.launch {
            isSearching = true
            errorMessage = null
            statusMessage = null
            video = null
            try {
                video = VideoApi.fetchVideo(videoUrl)
            } catch (exception: Exception) {
                errorMessage = exception.message ?: "ভিডিওর তথ্য আনা যায়নি। আবার চেষ্টা করুন।"
            } finally {
                isSearching = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 17.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.ic_downloader_mark),
                contentDescription = null,
                modifier = Modifier.size(50.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "VIDEO DOWNLOADER",
                    color = AppColors.text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.1.sp,
                )
                Text("লিংক পেস্ট করুন, বাকিটা সহজ", color = AppColors.muted, fontSize = 12.sp)
            }
            Surface(
                color = AppColors.panelRaised,
                shape = CircleShape,
                border = BorderStroke(1.dp, AppColors.border),
            ) {
                Text(
                    text = "HD",
                    color = AppColors.mint,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        HeroCard()

        Spacer(Modifier.height(17.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.panel),
            border = BorderStroke(1.dp, AppColors.border.copy(alpha = 0.65f)),
        ) {
            Column(Modifier.padding(17.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Link,
                        contentDescription = null,
                        tint = AppColors.cyan,
                        modifier = Modifier.size(19.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "ভিডিওর লিংক",
                        color = AppColors.text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "শুধু নিজের বা অনুমতি থাকা ভিডিও ব্যবহার করুন।",
                    color = AppColors.muted,
                    fontSize = 12.sp,
                )
                Spacer(Modifier.height(13.dp))

                OutlinedTextField(
                    value = videoUrl,
                    onValueChange = {
                        videoUrl = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://…", color = AppColors.muted) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Link, contentDescription = null, tint = AppColors.muted)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { searchVideo() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.mint,
                        unfocusedBorderColor = AppColors.border,
                        focusedTextColor = AppColors.text,
                        unfocusedTextColor = AppColors.text,
                        cursorColor = AppColors.mint,
                        focusedContainerColor = AppColors.background.copy(alpha = 0.45f),
                        unfocusedContainerColor = AppColors.background.copy(alpha = 0.45f),
                    ),
                )

                Spacer(Modifier.height(11.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(
                                Context.CLIPBOARD_SERVICE,
                            ) as ClipboardManager
                            val copiedText = clipboard.primaryClip
                                ?.takeIf { it.itemCount > 0 }
                                ?.getItemAt(0)
                                ?.coerceToText(context)
                                ?.toString()
                                .orEmpty()

                            if (copiedText.isNotBlank()) {
                                videoUrl = copiedText
                                errorMessage = null
                            } else {
                                errorMessage = "Clipboard-এ কোনো লিংক নেই।"
                            }
                        },
                        modifier = Modifier.weight(0.88f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.panelRaised,
                            contentColor = AppColors.text,
                        ),
                    ) {
                        Icon(Icons.Outlined.ContentPaste, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("পেস্ট", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { searchVideo() },
                        enabled = !isSearching,
                        modifier = Modifier.weight(1.35f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.mint,
                            contentColor = AppColors.darkInk,
                            disabledContainerColor = AppColors.mint.copy(alpha = 0.6f),
                        ),
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(19.dp),
                                color = AppColors.darkInk,
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("খোঁজা হচ্ছে…", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Outlined.Search, contentDescription = null)
                            Spacer(Modifier.width(7.dp))
                            Text("ভিডিও খুঁজুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        errorMessage?.let {
            Spacer(Modifier.height(13.dp))
            FeedbackBanner(message = it, color = AppColors.red)
        }
        statusMessage?.let {
            Spacer(Modifier.height(13.dp))
            FeedbackBanner(message = it, color = AppColors.mint)
        }

        video?.let { details ->
            Spacer(Modifier.height(17.dp))
            VideoResultCard(
                video = details,
                selectedUrl = selectedUrl,
                onQualitySelected = { selectedUrl = it },
                onDownload = {
                    selectedUrl?.let { startDownload(details.title, it) }
                },
            )
        }

        if (activeDownloadCount > 0) {
            Spacer(Modifier.height(14.dp))
            ActiveDownloadsCard(
                count = activeDownloadCount,
                onClick = onOpenDownloads,
            )
        }

        Spacer(Modifier.height(16.dp))
        DownloadFolderHint()
        Spacer(Modifier.height(17.dp))
    }
}

@Composable
private fun HeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF15446B), Color(0xFF122A4B), Color(0xFF14223A)),
                ),
            )
            .padding(21.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.78f)) {
            Surface(
                shape = CircleShape,
                color = AppColors.cyan.copy(alpha = 0.13f),
                border = BorderStroke(1.dp, AppColors.cyan.copy(alpha = 0.22f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .background(AppColors.mint, CircleShape),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        "দ্রুত ও সহজ",
                        color = AppColors.mint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "তোমার ভিডিও,\nএক জায়গায়।",
                color = AppColors.text,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 31.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "লিংক বিশ্লেষণ করে quality বেছে Download/Nayan ফোল্ডারে রাখো।",
                color = Color(0xFFC3D4E4),
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }
        Icon(
            imageVector = Icons.Outlined.CloudDownload,
            contentDescription = null,
            tint = AppColors.cyan.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(60.dp),
        )
    }
}

@Composable
private fun VideoResultCard(
    video: VideoDetails,
    selectedUrl: String?,
    onQualitySelected: (String) -> Unit,
    onDownload: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.panel),
        border = BorderStroke(1.dp, AppColors.border),
    ) {
        Column(Modifier.padding(15.dp)) {
            video.thumbnailUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "ভিডিওর থাম্বনেইল",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(18.dp)),
                )
                Spacer(Modifier.height(13.dp))
            }
            Text(
                "ভিডিও পাওয়া গেছে",
                color = AppColors.mint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                video.title,
                color = AppColors.text,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 23.sp,
            )
            Spacer(Modifier.height(14.dp))
            Text("Quality বেছে নাও", color = AppColors.muted, fontSize = 12.sp)
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                video.qualities.forEach { quality ->
                    QualityChip(
                        quality = quality,
                        selected = selectedUrl == quality.url,
                        onSelect = { onQualitySelected(quality.url) },
                    )
                }
            }
            Spacer(Modifier.height(13.dp))
            Button(
                onClick = onDownload,
                enabled = selectedUrl != null,
                modifier = Modifier.fillMaxWidth().height(53.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.mint,
                    contentColor = AppColors.darkInk,
                ),
            ) {
                Icon(Icons.Outlined.Download, contentDescription = null)
                Spacer(Modifier.width(9.dp))
                Text("Download করুন", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "ডাউনলোড লিংক কিছু সময় পর মেয়াদোত্তীর্ণ হতে পারে।",
                color = AppColors.muted,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun QualityChip(
    quality: DownloadQuality,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onSelect,
        label = { Text(quality.label) },
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
private fun ActiveDownloadsCard(
    count: Int,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = AppColors.panelRaised,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, AppColors.border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.Download,
                contentDescription = null,
                tint = AppColors.mint,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${count}টি download চলছে",
                    color = AppColors.text,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                )
                Text("Progress দেখতে Download ট্যাবে যাও", color = AppColors.muted, fontSize = 11.sp)
            }
            Icon(
                Icons.Outlined.FolderOpen,
                contentDescription = null,
                tint = AppColors.cyan,
            )
        }
    }
}

@Composable
private fun DownloadFolderHint() {
    Surface(
        color = AppColors.panel.copy(alpha = 0.78f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppColors.border.copy(alpha = 0.8f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.FolderOpen,
                contentDescription = null,
                tint = AppColors.cyan,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(9.dp))
            Column {
                Text("সংরক্ষণের জায়গা", color = AppColors.muted, fontSize = 10.sp)
                Text(
                    "Downloads/Nayan",
                    color = AppColors.text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun FeedbackBanner(message: String, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.11f),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
    ) {
        Text(
            message,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            color = color,
            fontSize = 12.sp,
            lineHeight = 18.sp,
        )
    }
}
