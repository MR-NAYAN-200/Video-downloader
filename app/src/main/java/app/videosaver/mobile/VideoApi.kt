package app.videosaver.mobile

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class DownloadQuality(
    val label: String,
    val url: String,
)

data class VideoDetails(
    val title: String,
    val thumbnailUrl: String?,
    val qualities: List<DownloadQuality>,
)

object VideoApi {
    private const val API_HOST = "nayan-video-downloader.vercel.app"
    private const val API_PATH = "/alldown"

    fun normalizeVideoUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        require(trimmed.isNotEmpty()) { "আগে একটি ভিডিও লিংক দিন।" }

        val withScheme = if ("://" in trimmed) trimmed else "https://$trimmed"
        val uri = Uri.parse(withScheme)
        require(
            uri.scheme.equals("https", ignoreCase = true) ||
                uri.scheme.equals("http", ignoreCase = true),
        ) { "শুধু http বা https ভিডিও লিংক ব্যবহার করুন।" }
        require(!uri.host.isNullOrBlank()) { "ভিডিও লিংকটি সঠিক নয়।" }
        return uri.toString()
    }

    suspend fun fetchVideo(rawUrl: String): VideoDetails = withContext(Dispatchers.IO) {
        val videoUrl = normalizeVideoUrl(rawUrl)
        val requestUri = Uri.Builder()
            .scheme("https")
            .authority(API_HOST)
            .appendPath(API_PATH.removePrefix("/"))
            .appendQueryParameter("url", videoUrl)
            .build()

        val connection = (URL(requestUri.toString()).openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 25_000
            connection.setRequestProperty("Accept", "application/json")

            val statusCode = connection.responseCode
            val responseStream = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val responseBody = responseStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (statusCode !in 200..299) {
                throw IOException("সার্ভার উত্তর দেয়নি (HTTP $statusCode)।")
            }

            val root = JSONObject(responseBody)
            if (!root.optBoolean("status")) {
                val message = root.optString("message").takeIf { it.isNotBlank() }
                throw IOException(message ?: "এই লিংক থেকে ভিডিও পাওয়া যায়নি।")
            }

            val data = root.optJSONObject("data")
                ?: throw IOException("সার্ভার থেকে ভিডিওর তথ্য পাওয়া যায়নি।")
            val lowUrl = data.optString("low").takeIf(::isHttpsUrl)
            val highUrl = data.optString("high").takeIf(::isHttpsUrl)
            val options = when {
                lowUrl != null && highUrl != null && lowUrl == highUrl ->
                    listOf(DownloadQuality("একটি ফাইল পাওয়া গেছে", lowUrl))
                lowUrl != null && highUrl != null ->
                    listOf(
                        DownloadQuality("স্ট্যান্ডার্ড", lowUrl),
                        DownloadQuality("উচ্চ মান", highUrl),
                    )
                highUrl != null -> listOf(DownloadQuality("উচ্চ মান", highUrl))
                lowUrl != null -> listOf(DownloadQuality("স্ট্যান্ডার্ড", lowUrl))
                else -> throw IOException("ডাউনলোডের জন্য কোনো ভিডিও লিংক পাওয়া যায়নি।")
            }

            VideoDetails(
                title = data.optString("title").takeIf { it.isNotBlank() } ?: "ভিডিও",
                thumbnailUrl = data.optString("thumbnail").takeIf(::isHttpsUrl),
                qualities = options,
            )
        } catch (exception: org.json.JSONException) {
            throw IOException("সার্ভারের উত্তরটি পড়া যায়নি।", exception)
        } finally {
            connection.disconnect()
        }
    }

    private fun isHttpsUrl(value: String): Boolean {
        val uri = Uri.parse(value)
        return uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
    }
}
