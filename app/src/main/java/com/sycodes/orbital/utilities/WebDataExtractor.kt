package com.sycodes.orbital.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object WebDataExtractor {

    fun extractWebData(webView: WebView, context: Context, tabId: Int): WebData {

            val title = webView.title ?: "New Tab"
            val faviconPath = saveFaviconToPrivateStorage(webView.favicon, context, tabId)
            val previewPath = saveWebPreviewToPrivateStorage(webView, context, tabId)

          return  WebData(title, faviconPath, previewPath)

    }

    private fun saveFaviconToPrivateStorage(bitmap: Bitmap?, context: Context, tabId: Int): String {
        if (bitmap == null) return ""

        val file = File(context.filesDir, "favicon_$tabId.png")

        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("WebDataExtractor", "Error saving favicon", e)
            ""
        }
    }

    private fun saveWebPreviewToPrivateStorage(webView: WebView, context: Context, tabId: Int): String {
        val bitmap = Bitmap.createBitmap(webView.width, webView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        webView.draw(canvas)

        val file = File(context.filesDir, "preview_$tabId.png")

        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("WebDataExtractor", "Error saving web preview", e)
            ""
        }
    }

    data class WebData(val title: String, val faviconPath: String, val previewPath: String)
}

