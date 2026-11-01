package app.videosaver.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.videosaver.mobile.ui.VideoDownloaderApp
import app.videosaver.mobile.ui.VideoDownloaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoDownloaderTheme {
                VideoDownloaderApp()
            }
        }
    }
}
