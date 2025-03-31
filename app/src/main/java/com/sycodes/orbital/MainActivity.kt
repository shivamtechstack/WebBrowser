package com.sycodes.orbital

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sycodes.orbital.databinding.ActivityMainBinding
import com.sycodes.orbital.models.TabDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        var tabDao = TabDatabase.getDatabase(this).tabDataDao()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        CoroutineScope(Dispatchers.IO).launch {
            val lastActiveTab = tabDao.getActiveTab()
            if (lastActiveTab != null) {
                val fragment = BrowserTabFragment.newInstance(lastActiveTab.url)
                runOnUiThread {
                    fragmentTransaction.replace(R.id.main_Fragment_Container, fragment)
                        .commit()
                }
            }else{
                runOnUiThread {
                    fragmentTransaction.replace(R.id.main_Fragment_Container, BrowserTabFragment())
                        .commit()
                }
            }
        }
    }

    override fun onBackPressed() {
        val browserFragment = supportFragmentManager.findFragmentById(R.id.main_Fragment_Container) as? BrowserTabFragment
        if (browserFragment?.canGoBack() == true) {
            browserFragment.goBack()
        } else {
            super.onBackPressed()
        }
    }
}