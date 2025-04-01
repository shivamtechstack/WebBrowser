package com.sycodes.orbital

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sycodes.orbital.databinding.ActivityMainBinding
import com.sycodes.orbital.models.TabData
import com.sycodes.orbital.models.TabDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        CoroutineScope(Dispatchers.IO).launch {
            val activeTab = TabDatabase.getDatabase(this@MainActivity).tabDataDao().getActiveTab()
            withContext(Dispatchers.Main) {
                val fragment = if (activeTab != null) {
                    BrowserTabFragment.newInstance(activeTab.url, activeTab.id)
                } else {
                    BrowserTabFragment.newInstance("")
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_Fragment_Container, fragment)
                    .commit()
            }
        }

    }
}