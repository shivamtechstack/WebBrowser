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
import androidx.appcompat.widget.PopupMenu
import com.sycodes.orbital.databinding.FragmentBrowserTabBinding
import com.sycodes.orbital.fragments.TabGroupFragment
import com.sycodes.orbital.models.TabData
import com.sycodes.orbital.models.TabDataDao
import com.sycodes.orbital.models.TabDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder

class BrowserTabFragment : Fragment() {
    private lateinit var binding: FragmentBrowserTabBinding
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var tabDao: TabDataDao

    companion object {
        private const val ARG_URL = "url"
        fun newInstance(url: String): BrowserTabFragment {
            val fragment = BrowserTabFragment()
            val args = Bundle()
            args.putString(ARG_URL, url)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBrowserTabBinding.inflate(inflater, container, false)

        webView = binding.webView
        progressBar = binding.progressBar
        val url = arguments?.getString(ARG_URL)

        setupWebView()

        tabDao = TabDatabase.getDatabase(requireContext()).tabDataDao()

        CoroutineScope(Dispatchers.IO).launch {
            val activeTab = tabDao.getActiveTab()
            requireActivity().runOnUiThread {
                if (activeTab != null) {
                    loadUrl(activeTab.url)
                } else {
                    webView.visibility = View.GONE
                    binding.homePageLayout.visibility = View.VISIBLE
                }
            }
        }

        if (!url.isNullOrBlank()) {
            binding.homePageLayout.visibility = View.GONE
            webView.visibility = View.VISIBLE
            loadUrl(url)
        }else{
            webView.visibility = View.GONE
            binding.homePageLayout.visibility = View.VISIBLE
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
        popupMenu.show()

    }

    private fun loadUrl(query: String) {
        val url = if (Patterns.WEB_URL.matcher(query).matches()) {
            if (!query.startsWith("http")) "https://$query" else query
        } else {
            "https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8")
        }
        binding.homePageLayout.visibility = View.GONE
        webView.visibility = View.VISIBLE
        webView.loadUrl(url)


        CoroutineScope(Dispatchers.IO).launch {
            tabDao.deactivateAllTabs()

            val existingTab = arguments?.getString(ARG_URL)

            if (existingTab.isNullOrBlank()) {
                tabDao.insertTabData(
                    TabData(url = url, title = "New Tab", favicon = " ", isActive = true)
                )
            } else {
                tabDao.updateTabData(
                    TabData(url = url, title = "Updated Tab", favicon = " ", isActive = true)
                )
            }
        }

    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                request?.url?.let { view?.loadUrl(it.toString()) }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.progressBar.visibility = View.VISIBLE
                webView.visibility = View.VISIBLE
                binding.searchTextEditText.setText(url)
                CoroutineScope(Dispatchers.IO).launch {
                    tabDao.updateTabData(TabData(url = url.toString(), title = "New Tab", favicon = " ", isActive = true))
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressBar.visibility = View.GONE

            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding.progressBar.progress = newProgress
                if (newProgress == 100) binding.progressBar.visibility = View.GONE
                else binding.progressBar.visibility = View.VISIBLE
            }
        }
    }

    private fun setUpBottomNavigation(){
        binding.newTabButtonMainActivity.setOnClickListener {
            val currentUrl = webView.url
            if (currentUrl == null ){
                Toast.makeText(requireContext(),"New Tab", Toast.LENGTH_SHORT).show()
            }else{
                val fragment = BrowserTabFragment.newInstance("")

                CoroutineScope(Dispatchers.IO).launch {
                    val tabData = TabData(url = currentUrl, title = "New Tab", favicon = " ", isActive = false)
                    tabDao.insertTabData(tabData)
                }
                parentFragmentManager.beginTransaction().replace(R.id.main_Fragment_Container,fragment)
                    .addToBackStack(null)
                    .commit()


            }
        }

        binding.tabGroupButton.setOnClickListener {
            val fragment = TabGroupFragment()
            parentFragmentManager.beginTransaction()
                .hide(this)
                .add(R.id.main_Fragment_Container,fragment)
                .addToBackStack(null).commit()
        }
    }

    fun canGoBack(): Boolean = webView.canGoBack()
    fun goBack() = webView.goBack()
    fun canGoForward(): Boolean = webView.canGoForward()
    fun goForward() = webView.goForward()

}