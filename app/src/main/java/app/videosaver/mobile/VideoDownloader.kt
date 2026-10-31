package app.videosaver.mobile

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

object VideoDownloader {
    fun enqueue(context: Context, title: String, url: String): Long {
        val uri = Uri.parse(url)
        require(uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()) {
            "ডাউনলোড লিংকটি নিরাপদ নয়।"
        }

        val filename = buildFilename(title)
        val request = DownloadManager.Request(uri)
            .setTitle(title)
            .setDescription("ভিডিওটি ডাউনলোড হচ্ছে")
            .setMimeType("video/mp4")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = manager.enqueue(request)
        check(downloadId > 0) { "ডাউনলোডটি queue-তে যোগ করা যায়নি।" }
        return downloadId
    }

    private fun buildFilename(title: String): String {
        val safeTitle = title
            .replace(Regex("""[^\p{L}\p{N}._ -]"""), "_")
            .replace(Regex("""\s+"""), " ")
            .trim()
            .take(90)
            .ifBlank { "video" }

        return if (safeTitle.endsWith(".mp4", ignoreCase = true)) {
            safeTitle
        } else {
            "$safeTitle.mp4"
        }
    }
}
