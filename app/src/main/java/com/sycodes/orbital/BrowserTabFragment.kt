package com.sycodes.orbital

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sycodes.orbital.databinding.FragmentBrowserTabBinding
import com.sycodes.orbital.fragments.BookmarksFragment
import com.sycodes.orbital.fragments.HistoryFragment
import com.sycodes.orbital.fragments.TabGroupFragment
import com.sycodes.orbital.models.AppDatabase
import com.sycodes.orbital.models.Bookmark
import com.sycodes.orbital.models.History
import com.sycodes.orbital.models.TabData
import com.sycodes.orbital.models.TabDatabase
import com.sycodes.orbital.utilities.WebDataExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URLEncoder

class BrowserTabFragment : Fragment() {
    private lateinit var binding: FragmentBrowserTabBinding
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private var tabId: Int = -1
    private val tabDatabase by lazy { TabDatabase.getDatabase(requireContext())}
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var isNavigatingBack = false
    private lateinit var appDatabase : AppDatabase
    private var lastSavedUrl: String? = null
    private var hasInitialLoadHappened = false


    companion object {
        private const val ARG_URL = "url"
        private const val ARG_TAB_ID = "tab_id"

        fun newInstance(url: String, tabId: Int = -1): BrowserTabFragment {
            return BrowserTabFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, url)
                    putInt(ARG_TAB_ID, tabId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateBack()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBrowserTabBinding.inflate(inflater, container, false)

        webView = binding.webView
        progressBar = binding.progressBar

        setupWebView()

        arguments?.let {
            tabId = it.getInt(ARG_TAB_ID, -1)
            val url = it.getString(ARG_URL, "")

            if (url.isNotEmpty()) {
                webView.loadUrl(url)
                binding.searchTextEditText.setText(url)
                binding.homePageLayout.visibility = View.GONE
                webView.visibility = View.VISIBLE
            }else{
                binding.searchTextEditText.setText("")
                binding.homePageLayout.visibility = View.VISIBLE
                webView.visibility = View.GONE
            }
        }

        setUpBottomNavigation()

        binding.toolbarMenuIcon.setOnClickListener { view ->
            showPopupMenu(view)
        }

        binding.searchTextEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                loadUrl(binding.searchTextEditText.text.toString().trim())
                hideKeyboard(v)
                true
            } else {
                false
            }
        }
        return binding.root
    }

    private fun showPopupMenu(view1: View) {
        val popupMenu = PopupMenu(requireContext(), view1)
        popupMenu.menuInflater.inflate(R.menu.toolbarmenu, popupMenu.menu)

        popupMenu.setForceShowIcon(true)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.toolbar_bookmarks -> {
                    (activity as MainActivity).openTabGroup(BookmarksFragment())
                    true
                }
                R.id.toolbar_history ->{
                    (activity as MainActivity).openTabGroup(HistoryFragment())
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun loadUrl(query: String) {
        if (query.isBlank()) return

        val url = if (Patterns.WEB_URL.matcher(query).matches()) {
            if (!query.startsWith("http")) "https://$query" else query
        } else {
            "https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8")
        }
        binding.homePageLayout.visibility = View.GONE
        webView.visibility = View.VISIBLE
        webView.loadUrl(url)

    }

    private fun hideKeyboard(view: View) {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
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
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()

                if (request?.isForMainFrame == false) {
                    (activity as MainActivity).addNewTab(url)
                    return true
                }

                view?.loadUrl(url)
                return false
            }


        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (url.isNullOrBlank()) return

                binding.progressBar.visibility = View.VISIBLE
                webView.visibility = View.VISIBLE
                binding.searchTextEditText.setText(url)

            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressBar.visibility = View.GONE

                if (url != null) {

                    if (isAdded && context != null) {

                        if (isNavigatingBack) {
                            isNavigatingBack = false
                            return
                        }

                        val webData = WebDataExtractor.extractWebData(webView, requireContext(), tabId)
                        saveTabData(url, webData.title, webData.faviconPath, webData.previewPath)
                        saveUrlToHistory(url)
                        updateBookmarkIcon(url)
                        saveHistory(url, webData.title, webData.faviconPath)
                    }
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding.progressBar.progress = newProgress
                if (newProgress == 100) binding.progressBar.visibility = View.GONE
                else binding.progressBar.visibility = View.VISIBLE
            }

            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                if (icon != null && isAdded && context != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val faviconPath = WebDataExtractor.saveFaviconToPrivateStorage(icon, requireContext(), tabId)
                        val tab = tabDatabase.tabDataDao().getTab(tabId)
                        tab?.let {
                            val updatedTab = it.copy(favicon = faviconPath)
                            tabDatabase.tabDataDao().updateTabData(updatedTab)
                        }
                    }
                }
            }
        }
    }

    private fun saveTabData(url: String, title: String, faviconPath: String, previewPath: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (tabId == -1) {
                val newTab = TabData(url = url, title = title, favicon = faviconPath, urlPreview = previewPath, isActive = true)
                tabId = tabDatabase.tabDataDao().insertTabData(newTab).toInt()
            } else {
                val tab = tabDatabase.tabDataDao().getTab(tabId)
                tab?.let {
                    val updatedTab = it.copy(
                        url = url,
                        title = title,
                        favicon = faviconPath,
                        urlPreview = previewPath,
                        lastVisited = System.currentTimeMillis(),
                        isActive = true
                    )
                    tabDatabase.tabDataDao().updateTabData(updatedTab)
                }
            }
        }
    }

    fun saveHistory(url: String, title: String, faviconPath: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val history = History(url = url, title = title, favicon = faviconPath)
            AppDatabase.getAppDatabase(requireContext()).appDataDao().insertHistory(history)
        }
    }

    private fun setUpBottomNavigation() {
        binding.newTabButtonMainActivity.setOnClickListener {
            (activity as? MainActivity)?.addNewTab()
        }

        binding.tabGroupButton.setOnClickListener {
            (activity as? MainActivity)?.openTabGroup(TabGroupFragment())
        }

        binding.webViewGoBack.setOnClickListener {
            navigateBack()
        }

        binding.saveBookmarkButton.setOnClickListener {
            val url = webView.url ?: return@setOnClickListener

            CoroutineScope(Dispatchers.Main).launch {
                appDatabase = AppDatabase.getAppDatabase(requireContext())

                val existingBookmark = withContext(Dispatchers.IO) {
                    appDatabase.appDataDao().getBookmarkByUrl(url)
                }

                if (existingBookmark != null) {
                    withContext(Dispatchers.IO) {
                        appDatabase.appDataDao().deleteBookmarkByUrl(existingBookmark)
                    }
                    binding.saveBookmarkButton.setImageResource(R.drawable.bookmark_empty_24)
                    Toast.makeText(requireContext(), "Bookmark removed", Toast.LENGTH_SHORT).show()
                } else {
                    val webData = withContext(Dispatchers.Main) {
                        WebDataExtractor.extractWebData(webView, requireContext(), tabId)
                    }

                    val newBookmark = Bookmark(
                        url = url,
                        title = webData.title,
                        favicon = webData.faviconPath
                    )

                    withContext(Dispatchers.IO) {
                        appDatabase.appDataDao().insertBookmark(newBookmark)
                    }

                    binding.saveBookmarkButton.setImageResource(R.drawable.bookmark_24)
                    Toast.makeText(requireContext(), "Bookmark saved", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun navigateBack(){
        CoroutineScope(Dispatchers.IO).launch {
            val tab = tabDatabase.tabDataDao().getTab(tabId)
            tab?.let {
                val historyList = it.historyUrls.toUrlList().toMutableList()
                val currentIndex = it.historyIndex

                if (currentIndex > 0) {
                    historyList.removeAt(currentIndex)

                    val newIndex = currentIndex - 1
                    val urlToLoad = historyList[newIndex]

                    val updatedTab = it.copy(
                        historyUrls = historyList.toJsonString(),
                        historyIndex = newIndex
                    )
                    tabDatabase.tabDataDao().updateTabData(updatedTab)

                    isNavigatingBack = true

                    withContext(Dispatchers.Main) {
                        webView.loadUrl(urlToLoad)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "No more history", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun saveUrlToHistory(url: String) {
        if (isNavigatingBack) return
        CoroutineScope(Dispatchers.IO).launch {
            if (url == lastSavedUrl) return@launch

            val tab = tabDatabase.tabDataDao().getTab(tabId)
            tab?.let {
                val historyList = it.historyUrls.toUrlList()
                val currentIndex = it.historyIndex

                if (historyList.getOrNull(currentIndex) == url) return@launch

                val newHistory = historyList.take(currentIndex + 1).toMutableList()
                newHistory.add(url)

                lastSavedUrl = url

                val updatedTab = it.copy(
                    historyUrls = newHistory.toJsonString(),
                    historyIndex = newHistory.size - 1
                )
                tabDatabase.tabDataDao().updateTabData(updatedTab)
            }
        }
    }

    private fun updateBookmarkIcon(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            appDatabase = AppDatabase.getAppDatabase(requireContext())
            val existingBookmark = appDatabase.appDataDao().getBookmarkByUrl(url)

            withContext(Dispatchers.Main) {
                val iconRes = if (existingBookmark != null) {
                    R.drawable.bookmark_24
                } else {
                    R.drawable.bookmark_empty_24
                }
                binding.saveBookmarkButton.setImageResource(iconRes)
            }
        }
    }


    fun String.toUrlList(): List<String> {
        return try {
            JSONArray(this).let { array ->
                List(array.length()) { i -> array.getString(i) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun List<String>.toJsonString(): String {
        val array = JSONArray()
        this.forEach { array.put(it) }
        return array.toString()
    }

    override fun onResume() {
        super.onResume()
        backPressedCallback.isEnabled = true
    }

    override fun onPause() {
        super.onPause()
        backPressedCallback.isEnabled = false
    }
}