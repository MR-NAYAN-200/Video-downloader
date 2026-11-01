package app.videosaver.mobile

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import java.io.File
import java.io.IOException

enum class DownloadState {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
}

data class DownloadItem(
    val id: Long,
    val title: String,
    val state: DownloadState,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val reason: Int,
    val localUri: Uri?,
) {
    val progress: Float?
        get() = if (totalBytes > 0L) {
            (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
        } else {
            null
        }

    val isActive: Boolean
        get() = state == DownloadState.PENDING ||
            state == DownloadState.DOWNLOADING ||
            state == DownloadState.PAUSED
}

object VideoDownloader {
    const val DOWNLOAD_FOLDER = "Nayan"

    fun enqueue(context: Context, title: String, url: String): Long {
        val uri = Uri.parse(url)
        require(uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()) {
            "ডাউনলোড লিংকটি নিরাপদ নয়।"
        }

        ensureLegacyDownloadFolder()
        val filename = buildFilename(title)
        val request = DownloadManager.Request(uri)
            .setTitle(title)
            .setDescription("Video Downloader • Downloads/$DOWNLOAD_FOLDER")
            .setMimeType("video/mp4")
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED,
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "$DOWNLOAD_FOLDER/$filename",
            )

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = manager.enqueue(request)
        check(downloadId > 0) { "ডাউনলোডটি queue-তে যোগ করা যায়নি।" }
        return downloadId
    }

    fun listDownloads(context: Context): List<DownloadItem> {
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cursor = manager.query(DownloadManager.Query()) ?: return emptyList()

        return cursor.use { results ->
            val idColumn = results.getColumnIndexOrThrow(DownloadManager.COLUMN_ID)
            val titleColumn = results.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE)
            val statusColumn = results.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            val reasonColumn = results.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON)
            val downloadedColumn =
                results.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val totalColumn =
                results.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            val localUriColumn =
                results.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)
            val items = mutableListOf<DownloadItem>()

            while (results.moveToNext()) {
                val state = when (results.getInt(statusColumn)) {
                    DownloadManager.STATUS_PENDING -> DownloadState.PENDING
                    DownloadManager.STATUS_RUNNING -> DownloadState.DOWNLOADING
                    DownloadManager.STATUS_PAUSED -> DownloadState.PAUSED
                    DownloadManager.STATUS_SUCCESSFUL -> DownloadState.COMPLETED
                    else -> DownloadState.FAILED
                }
                val localUriText = results.getString(localUriColumn)

                items += DownloadItem(
                    id = results.getLong(idColumn),
                    title = results.getString(titleColumn).orEmpty(),
                    state = state,
                    downloadedBytes = results.getLong(downloadedColumn).coerceAtLeast(0L),
                    totalBytes = results.getLong(totalColumn).coerceAtLeast(0L),
                    reason = results.getInt(reasonColumn),
                    localUri = localUriText?.takeIf(String::isNotBlank)?.let(Uri::parse),
                )
            }

            items.sortedByDescending(DownloadItem::id)
        }
    }

    fun openDownload(context: Context, item: DownloadItem) {
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = manager.getUriForDownloadedFile(item.id)
            ?: item.localUri
            ?: throw IOException("ডাউনলোড করা ফাইলটি খুঁজে পাওয়া যায়নি।")

        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "video/*")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun failureMessage(reason: Int): String = when (reason) {
        DownloadManager.ERROR_INSUFFICIENT_SPACE -> "ফোনে জায়গা নেই।"
        DownloadManager.ERROR_FILE_ERROR -> "ফাইলটি সংরক্ষণ করা যায়নি।"
        DownloadManager.ERROR_UNHANDLED_HTTP_CODE,
        DownloadManager.ERROR_HTTP_DATA_ERROR -> "সার্ভার থেকে ভিডিওটি পাওয়া যায়নি।"
        DownloadManager.ERROR_CANNOT_RESUME -> "ডাউনলোডটি আবার শুরু করা যায়নি।"
        else -> "ডাউনলোড সম্পন্ন হয়নি। আবার চেষ্টা করুন।"
    }

    private fun ensureLegacyDownloadFolder() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) return

        val downloads = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS,
        )
        val folder = File(downloads, DOWNLOAD_FOLDER)
        if (!folder.exists() && !folder.mkdirs()) {
            throw IOException("Downloads/$DOWNLOAD_FOLDER ফোল্ডার তৈরি করা যায়নি।")
        }
    }

    private fun buildFilename(title: String): String {
        val safeTitle = title
            .replace(Regex("""[^\p{L}\p{N}._ -]"""), "_")
            .replace(Regex("""\s+"""), " ")
            .trim()
            .take(75)
            .ifBlank { "video" }

        val baseName = safeTitle.removeSuffix(".mp4").removeSuffix(".MP4")
        return "$baseName-${System.currentTimeMillis()}.mp4"
    }
}
