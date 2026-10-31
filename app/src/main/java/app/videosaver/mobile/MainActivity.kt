package app.videosaver.mobile

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

private val pageBackground = Color(0xFF0A1018)
private val surfaceColor = Color(0xFF141E29)
private val accentColor = Color(0xFF68E0C0)
private val primaryText = Color(0xFFF4F7FA)
private val secondaryText = Color(0xFFA2B0BE)
private val errorColor = Color(0xFFFF8D8D)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = accentColor,
                    onPrimary = Color(0xFF071711),
                    background = pageBackground,
                    surface = surfaceColor,
                    onBackground = primaryText,
                    onSurface = primaryText,
                    error = errorColor,
                ),
            ) {
                VideoSaverScreen()
            }
        }
    }
}

@Composable
private fun VideoSaverScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var videoUrl by rememberSaveable { mutableStateOf("") }
    var video by remember { mutableStateOf<VideoDetails?>(null) }
    var selectedUrl by remember(video) {
        mutableStateOf(video?.qualities?.lastOrNull()?.url)
    }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var pendingDownloadUrl by remember { mutableStateOf<String?>(null) }

    val startDownload: (String) -> Unit = { url ->
        val currentVideo = video
        if (currentVideo == null) {
            errorMessage = "আগে ভিডিওর তথ্য সংগ্রহ করুন।"
        } else {
            try {
                VideoDownloader.enqueue(context, currentVideo.title, url)
                errorMessage = null
                statusMessage = "ডাউনলোড শুরু হয়েছে। Downloads ফোল্ডারে পাবেন।"
            } catch (exception: Exception) {
                errorMessage = exception.message ?: "ডাউনলোড শুরু করা যায়নি।"
                statusMessage = null
            }
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val url = pendingDownloadUrl
        pendingDownloadUrl = null
        if (granted && url != null) {
            startDownload(url)
        } else {
            errorMessage = "ডাউনলোডের জন্য স্টোরেজ অনুমতি দরকার।"
        }
    }

    fun analyzeVideo() {
        scope.launch {
            isLoading = true
            errorMessage = null
            statusMessage = null
            video = null
            try {
                video = VideoApi.fetchVideo(videoUrl)
            } catch (exception: Exception) {
                errorMessage = exception.message ?: "ভিডিওর তথ্য আনা যায়নি। আবার চেষ্টা করুন।"
            } finally {
                isLoading = false
            }
        }
    }

    fun requestDownload(url: String) {
        if (
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingDownloadUrl = url
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            startDownload(url)
        }
    }

    Scaffold(containerColor = pageBackground) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(accentColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("▶", color = Color(0xFF071711), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(13.dp))
                Column {
                    Text(
                        "ভিডিও সেভার",
                        color = primaryText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("লিংক দিন, ভিডিওটি সংরক্ষণ করুন", color = secondaryText, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(26.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        "ভিডিওর লিংক",
                        color = primaryText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "যে ভিডিওটি সংরক্ষণের অনুমতি আপনার আছে, তার লিংক পেস্ট করুন।",
                        color = secondaryText,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    )
                    Spacer(Modifier.height(15.dp))

                    OutlinedTextField(
                        value = videoUrl,
                        onValueChange = {
                            videoUrl = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("https://…", color = secondaryText) },
                        singleLine = true,
                        shape = RoundedCornerShape(15.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { analyzeVideo() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color(0xFF344251),
                            focusedTextColor = primaryText,
                            unfocusedTextColor = primaryText,
                            cursorColor = accentColor,
                        ),
                    )

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val text = clipboard.primaryClip
                                    ?.takeIf { it.itemCount > 0 }
                                    ?.getItemAt(0)
                                    ?.coerceToText(context)
                                    ?.toString()
                                    .orEmpty()
                                if (text.isNotBlank()) {
                                    videoUrl = text
                                    errorMessage = null
                                } else {
                                    errorMessage = "Clipboard-এ কোনো লিংক নেই।"
                                }
                            },
                            modifier = Modifier.weight(0.9f).height(52.dp),
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF263442),
                                contentColor = primaryText,
                            ),
                        ) {
                            Text("পেস্ট", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { analyzeVideo() },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1.4f).height(52.dp),
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = Color(0xFF071711),
                                disabledContainerColor = Color(0xFF536B65),
                            ),
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color(0xFF071711),
                                    strokeWidth = 2.dp,
                                )
                                Spacer(Modifier.width(9.dp))
                            }
                            Text(
                                if (isLoading) "খোঁজা হচ্ছে…" else "ভিডিও খুঁজুন",
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            errorMessage?.let { message ->
                Spacer(Modifier.height(14.dp))
                MessageCard(message = message, color = errorColor)
            }

            statusMessage?.let { message ->
                Spacer(Modifier.height(14.dp))
                MessageCard(message = message, color = accentColor)
            }

            video?.let { result ->
                Spacer(Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        result.thumbnailUrl?.let { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "ভিডিওর থাম্বনেইল",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(190.dp)
                                    .clip(RoundedCornerShape(17.dp)),
                            )
                            Spacer(Modifier.height(15.dp))
                        }

                        Text(
                            result.title,
                            color = primaryText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 25.sp,
                        )
                        Spacer(Modifier.height(15.dp))
                        Text("ডাউনলোড অপশন", color = secondaryText, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            result.qualities.forEach { option ->
                                FilterChip(
                                    selected = selectedUrl == option.url,
                                    onClick = { selectedUrl = option.url },
                                    label = { Text(option.label) },
                                    shape = RoundedCornerShape(12.dp),
                                )
                            }
                        }

                        Spacer(Modifier.height(11.dp))
                        Button(
                            onClick = {
                                selectedUrl?.let(::requestDownload)
                            },
                            enabled = selectedUrl != null,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = Color(0xFF071711),
                            ),
                        ) {
                            Text("ডাউনলোড করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Spacer(Modifier.height(10.dp))
                        Text(
                            "ডাউনলোড লিংক অল্প সময়ে মেয়াদোত্তীর্ণ হতে পারে।",
                            color = secondaryText,
                            fontSize = 12.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(7.dp)
                        .background(accentColor, CircleShape),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "শুধু নিজের বা অনুমতি থাকা ভিডিও ডাউনলোড করুন।",
                    color = secondaryText,
                    fontSize = 12.sp,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MessageCard(message: String, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(15.dp),
    ) {
        Text(
            message,
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
            color = color,
            fontSize = 13.sp,
            lineHeight = 19.sp,
        )
    }
}
