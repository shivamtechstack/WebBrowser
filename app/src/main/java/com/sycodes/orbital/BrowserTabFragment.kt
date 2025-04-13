package com.sycodes.orbital

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sycodes.orbital.adapters.ShortcutAdapter
import com.sycodes.orbital.databinding.FragmentBrowserTabBinding
import com.sycodes.orbital.fragments.TabGroupFragment
import com.sycodes.orbital.menus.ToolBarPopUpMenu
import com.sycodes.orbital.models.AppDatabase
import com.sycodes.orbital.models.Bookmark
import com.sycodes.orbital.models.History
import com.sycodes.orbital.models.TabData
import com.sycodes.orbital.models.TabDatabase
import com.sycodes.orbital.utilities.WebPageMetaExtractor
import com.sycodes.orbital.utilities.WebViewConfigurator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URLEncoder

class BrowserTabFragment : Fragment() {
    private lateinit var binding: FragmentBrowserTabBinding
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private var tabId: Int = -1
    private lateinit var tabDatabase: TabDatabase
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var isNavigatingBack = false
    private lateinit var appDatabase : AppDatabase
    private var lastSavedUrl: String? = null


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

    override fun onAttach(context: Context) {
        super.onAttach(context)
            tabDatabase = TabDatabase.getDatabase(context)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBrowserTabBinding.inflate(inflater, container, false)

        webView = binding.webView
        progressBar = binding.progressBar



        arguments?.let {
            tabId = it.getInt(ARG_TAB_ID, -1)
            val url = it.getString(ARG_URL, "")

            setupWebView()

            if (url.isNotEmpty()) {
                webView.loadUrl(url)
                binding.searchTextEditText.setText(url)
                binding.homePageLayout.visibility = View.GONE
                webView.visibility = View.VISIBLE
            }else{
                binding.searchTextEditText.setText("")
                binding.homePageLayout.visibility = View.VISIBLE
                webView.visibility = View.GONE

                CoroutineScope(Dispatchers.IO).launch {
                    val shortcuts =
                        AppDatabase.getAppDatabase(requireContext()).appDataDao().getAllShortcuts()
                    if (shortcuts.isNotEmpty()) {

                        withContext(Dispatchers.Main) {
                            binding.recyclerViewShortcuts.visibility = View.VISIBLE
                            binding.recyclerViewShortcuts.adapter = ShortcutAdapter(shortcuts, onShortcutClickListener = {
                                loadUrl(it.url)
                            })
                            binding.recyclerViewShortcuts.layoutManager = GridLayoutManager(requireContext(),2,
                                RecyclerView.HORIZONTAL, false)
                        }
                    }
                }
            }
        }

        setUpBottomNavigation()

        binding.toolbarMenuIcon.setOnClickListener { view ->
            ToolBarPopUpMenu.showPopupMenu(this,view, webView)
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

    private fun setupWebView() {
        WebViewConfigurator(
            context = requireContext(),
            webView = webView,
            progressBar = progressBar,
            searchEditText = binding.searchTextEditText,
            tabId = tabId,
            onNewTabRequested = { url -> (activity as MainActivity).addNewTab(url) },
            onSaveTabData = { url, title, faviconPath, previewPath ->
                saveTabData(url, title, faviconPath, previewPath)
            },
            onSaveHistory = { url, title, faviconPath ->
                saveHistory(url, title, faviconPath)
            },
            onUpdateBookmarkIcon = { url -> updateBookmarkIcon(url) },
            onSaveUrlToHistoryForGoBack = { url -> saveUrlToHistoryForWebViewGoBack(url) },
            getIsNavigatingBack = { isNavigatingBack },
            setIsNavigatingBack = { isNavigatingBack = it },
            lifecycleScope = lifecycleScope
        ).configure()
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

            if (!isAdded) return@launch
            val history = History(url = url, title = title, favicon = "")
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

                    val newBookmark = Bookmark(
                        url = url,
                        title = webView.title.toString(),
                        favicon = ""
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

    fun navigateBack() {
        CoroutineScope(Dispatchers.IO).launch {
            val tab = tabDatabase.tabDataDao().getTab(tabId)
            if (tab == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "No tab data found", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val historyList = tab.historyUrls.toUrlList().toMutableList()
            if (historyList.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "No history available", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val currentUrl =  withContext(Dispatchers.Main) {
                webView.url ?: ""
            }
            var currentIndex = historyList.indexOf(currentUrl)
            if (currentIndex == -1) {
                currentIndex = historyList.size - 1
            }

            if (currentIndex <= 0) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "No more history", Toast.LENGTH_SHORT).show()
                    WebPageMetaExtractor.deleteTabFavicon(requireContext(),tabId)
                    WebPageMetaExtractor.deleteTabPreview(requireContext(),tabId)
                    tabDatabase.tabDataDao().deleteTab(tabId)
                    (activity as? MainActivity)?.addNewTab()
                }
                return@launch
            }

            val newIndex = currentIndex - 1
            val urlToLoad = historyList.getOrNull(newIndex)
            if (urlToLoad.isNullOrBlank()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "No valid URL to navigate back", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            historyList.removeAt(currentIndex)

            val updatedTab = tab.copy(
                historyUrls = historyList.toJsonString(),
                historyIndex = newIndex
            )
            tabDatabase.tabDataDao().updateTabData(updatedTab)
            isNavigatingBack = true

            withContext(Dispatchers.Main) {
                try {
                    webView.loadUrl(urlToLoad)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Failed to load URL", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun saveUrlToHistoryForWebViewGoBack(url: String) {
        if (isNavigatingBack) return

        CoroutineScope(Dispatchers.IO).launch {
            val tab = tabDatabase.tabDataDao().getTab(tabId) ?: return@launch
            val historyList = tab.historyUrls.toUrlList().toMutableList()

            if (historyList.contains(url)) return@launch

            historyList.add(url)
            lastSavedUrl = url

            val updatedTab = tab.copy(
                historyUrls = historyList.toJsonString(),
                historyIndex = historyList.size - 1
            )
            tabDatabase.tabDataDao().updateTabData(updatedTab)
        }
    }


    private fun updateBookmarkIcon(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (!isAdded) return@launch
            val db = AppDatabase.getAppDatabase(requireContext())
            val existingBookmark = db.appDataDao().getBookmarkByUrl(url)

            withContext(Dispatchers.Main) {
                if (!isAdded) return@withContext
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