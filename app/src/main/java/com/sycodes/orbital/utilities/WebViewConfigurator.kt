package com.sycodes.orbital.utilities

import com.sycodes.orbital.models.TabDatabase

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.*
import android.widget.EditText
import android.widget.ProgressBar
import kotlinx.coroutines.*

class WebViewConfigurator(
    private val context: Context,
    private val webView: WebView,
    private val progressBar: ProgressBar,
    private val searchEditText: EditText,
    private val tabId: Int,
    private val onNewTabRequested: (url: String) -> Unit,
    private val onSaveTabData: (url: String, title: String, faviconPath: String, previewPath: String) -> Unit,
    private val onSaveHistory: (url: String, title: String, faviconPath: String) -> Unit,
    private val onUpdateBookmarkIcon: (url: String) -> Unit,
    private val onSaveUrlToHistoryForGoBack: (url: String) -> Unit,
    private val getIsNavigatingBack: () -> Boolean,
    private val setIsNavigatingBack: (Boolean) -> Unit,
    private val lifecycleScope: CoroutineScope
) {

    @SuppressLint("SetJavaScriptEnabled")
    fun configure() {
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.databaseEnabled = true
        webSettings.setSupportMultipleWindows(true)
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (request?.isForMainFrame == false) {
                    onNewTabRequested(url)
                    return true
                }
                view?.loadUrl(url)
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (url.isNullOrBlank()) return
                progressBar.visibility = ProgressBar.VISIBLE
                webView.visibility = WebView.VISIBLE
                searchEditText.setText(url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = ProgressBar.GONE
                if (url != null) {
                    if (getIsNavigatingBack()) {
                        setIsNavigatingBack(false)
                        return
                    }

                    val webData = WebDataExtractor.extractWebData(webView, context, tabId)
                    onSaveTabData(url, webData.title, webData.faviconPath, webData.previewPath)
                    onSaveUrlToHistoryForGoBack(url)
                    onUpdateBookmarkIcon(url)
                    onSaveHistory(url, webData.title, webData.faviconPath)
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress == 100) ProgressBar.GONE else ProgressBar.VISIBLE
            }

            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                if (icon != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val faviconPath = WebDataExtractor.saveFaviconToPrivateStorage(
                            icon, context, WebDataExtractor.FaviconType.TAB, tabId.toString()
                        )
                        val tabDao = TabDatabase.getDatabase(context).tabDataDao()
                        val tab = tabDao.getTab(tabId)
                        tab?.let {
                            val updatedTab = it.copy(favicon = faviconPath)
                            tabDao.updateTabData(updatedTab)
                        }
                    }
                }
            }
        }
    }
}
