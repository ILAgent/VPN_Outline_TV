package com.ilagent.nativeoutline.data.remote

import android.os.Build
import com.ilagent.nativeoutline.utils.CrashlyticsLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

interface RemoteJSONFetch {
    suspend fun fetch(urlString: String): String

    class HttpURLConnectionJSONFetch : RemoteJSONFetch {

        override suspend fun fetch(urlString: String): String = withContext(Dispatchers.IO) {
            val url = URL(urlString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty(
                "User-Agent",
                "Android/${Build.VERSION.RELEASE} (${Build.MODEL}; ${Build.MANUFACTURER})"
            )
            connection.connect()

            if (connection.responseCode == 429) {
                throw Exception("Too many requests: ${connection.responseCode}")
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Failed to fetch: ${connection.responseCode}")
            }

            if (connection.contentType != "application/json") {
                throw Exception("Invalid content type: ${connection.responseCode} ${connection.contentType}")
            }

            return@withContext connection.inputStream.bufferedReader().use { it.readText() }
        }
    }
}

/**
 * Interface for checking URL availability (e.g., image URLs for flags)
 */
interface UrlChecker {
    suspend fun isUrlAvailable(urlString: String): Boolean

    class HttpUrlChecker : UrlChecker {
        override suspend fun isUrlAvailable(urlString: String): Boolean =
            withContext(Dispatchers.IO) {
                try {
                    val url = URL(urlString)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "HEAD"
                    connection.setRequestProperty(
                        "User-Agent",
                        "Android/${Build.VERSION.RELEASE} (${Build.MODEL}; ${Build.MANUFACTURER})"
                    )
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.connect()

                    val responseCode = connection.responseCode
                    connection.disconnect()

                    responseCode == HttpURLConnection.HTTP_OK
                } catch (e: Exception) {
                    CrashlyticsLogger.logException(e, "Flag check failed: $urlString")
                    false
                }
            }
    }
}