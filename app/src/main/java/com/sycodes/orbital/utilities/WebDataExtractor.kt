package com.sycodes.orbital.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.webkit.WebView
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object WebDataExtractor {

    fun extractWebData(webView: WebView, context: Context, tabId: Int): WebData {
        val title = webView.title ?: ""
        val previewPath = ScreenshotUtils.captureWebView(webView, context, tabId)
        return WebData(title, "", previewPath)
    }

    enum class FaviconType {
        TAB, BOOKMARK, HISTORY
    }

    fun saveFaviconToPrivateStorage(bitmap: Bitmap, context: Context, type: FaviconType, identifier: String): String {
        val dir = File(context.filesDir, "favicons")
        if (!dir.exists()) dir.mkdirs()

        val fileName = when (type) {
            FaviconType.TAB -> "tab_favicon_${identifier}.png"
            FaviconType.BOOKMARK -> "bookmark_favicon_${identifier.hashCode()}.png"
            FaviconType.HISTORY -> "history_favicon_${identifier.hashCode()}.png"
        }

        val file = File(dir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        return file.absolutePath
    }

    data class WebData(val title: String, val faviconPath: String, val previewPath: String)

    object ScreenshotUtils {

        fun captureWebView(webView: WebView, context: Context, tabId: Int): String {
            return try {
                val bitmap = Bitmap.createBitmap(
                    webView.width,
                    webView.height,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                webView.draw(canvas)

                val dir = File(context.filesDir, "previews")
                if (!dir.exists()) dir.mkdirs()

                val file = File(dir, "preview_tab_$tabId.png")
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                out.flush()
                out.close()

                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }

    fun saveFaviconForBookmark(bitmap: Bitmap, context: Context, url: String): String {
        return saveFaviconToPrivateStorage(bitmap, context, WebDataExtractor.FaviconType.BOOKMARK, url)
    }

    fun saveFaviconForHistory(bitmap: Bitmap, context: Context, url: String): String {
        return saveFaviconToPrivateStorage(bitmap, context, WebDataExtractor.FaviconType.HISTORY, url)
    }

    fun deleteFaviconFile(path: String): Boolean {
        return try {
            val file = File(path)
            file.exists() && file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteTabData(context: Context, tabId: Int) {
        val filesToDelete = listOf(
            File(context.filesDir, "previews/preview_tab_$tabId.png"),
            File(context.filesDir, "favicons/tab_favicon_$tabId.png")
        )

        filesToDelete.forEach { file ->
            if (file.exists()) file.delete()
        }
    }

}

