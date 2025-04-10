package com.sycodes.orbital.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.webkit.WebView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object WebPageMetaExtractor {

    fun extractTitle(webView: WebView): String {
        return webView.title ?: ""
    }

    fun extractFavicon(favicon: Bitmap?, context: Context, name: String): String {
        if (favicon == null) return ""

        val filename = "favicon_$name.png"
        val file = File(context.filesDir, filename)

        try {
            FileOutputStream(file).use { out ->
                favicon.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }

        return file.absolutePath
    }

    fun capturePreview(webView: WebView, context: Context, name: String): String {
        val filename = "$name.png"
        val file = File(context.filesDir, filename)

        val bitmap = Bitmap.createBitmap(webView.width, webView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        webView.draw(canvas)

        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }

        return file.absolutePath
    }

    fun deleteTabFavicon(context: Context, tabId: Int): Boolean {
        return safeDeleteFile(File(context.filesDir, "favicon_$tabId.png"))
    }

    fun deleteTabPreview(context: Context, tabId: Int): Boolean {
        return safeDeleteFile(File(context.filesDir, "$tabId.png"))
    }

    fun deleteHistoryFavicon(context: Context, name: String): Boolean {
        return safeDeleteFile(File(context.filesDir, "favicon_history_$name.png"))
    }

    fun deleteBookmarkFavicon(context: Context, name: String): Boolean {
        return safeDeleteFile(File(context.filesDir, "favicon_bookmark_$name.png"))
    }

    private fun safeDeleteFile(file: File): Boolean {
        return try {
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}


