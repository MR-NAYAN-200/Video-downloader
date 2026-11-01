package app.videosaver.mobile.ui

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import app.videosaver.mobile.DownloadItem
import app.videosaver.mobile.VideoDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private enum class MainTab(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Outlined.Home),
    DOWNLOADS("Download", Icons.Outlined.Download),
    INFO("Info", Icons.Outlined.Info),
}

@Composable
fun VideoDownloaderApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var downloadItems by remember { mutableStateOf(emptyList<DownloadItem>()) }

    LaunchedEffect(Unit) {
        delay(1_700)
        showSplash = false
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                downloadItems = withContext(Dispatchers.IO) {
                    VideoDownloader.listDownloads(context)
                }
                delay(900)
            }
        }
    }

    Crossfade(targetState = showSplash, label = "launch transition") { isSplash ->
        if (isSplash) {
            LaunchSplashScreen()
        } else {
            MainNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                downloadItems = downloadItems,
                onOpenDownload = { item ->
                    runCatching { VideoDownloader.openDownload(context, item) }
                        .onFailure {
                            Toast.makeText(
                                context,
                                it.message ?: "ভিডিওটি খোলা যায়নি।",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                },
            )
        }
    }
}

@Composable
private fun MainNavigation(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    downloadItems: List<DownloadItem>,
    onOpenDownload: (DownloadItem) -> Unit,
) {
    val stateHolder = rememberSaveableStateHolder()

    Scaffold(
        containerColor = AppColors.background,
        bottomBar = {
            NavigationBar(
                containerColor = AppColors.panel,
                tonalElevation = 0.dp,
            ) {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        icon = {
                            androidx.compose.material3.Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                            )
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppColors.mint,
                            selectedTextColor = AppColors.mint,
                            indicatorColor = AppColors.panelRaised,
                            unselectedIconColor = AppColors.muted,
                            unselectedTextColor = AppColors.muted,
                        ),
                    )
                }
            }
        },
    ) { insets ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.backgroundBrush)
                .padding(insets),
        ) {
            stateHolder.SaveableStateProvider(selectedTab.name) {
                when (selectedTab) {
                    MainTab.HOME -> HomeScreen(
                        activeDownloadCount = downloadItems.count { it.isActive },
                        onOpenDownloads = { onTabSelected(MainTab.DOWNLOADS) },
                    )
                    MainTab.DOWNLOADS -> DownloadsScreen(
                        items = downloadItems,
                        onOpenDownload = onOpenDownload,
                    )
                    MainTab.INFO -> InfoScreen()
                }
            }
        }
    }
}
