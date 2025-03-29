package com.sycodes.orbital

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import com.sycodes.orbital.databinding.FragmentBrowserTabBinding

class BrowserTabFragment : Fragment() {
    private lateinit var binding: FragmentBrowserTabBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBrowserTabBinding.inflate(inflater, container, false)

        binding.toolbarMenuIcon.setOnClickListener { view ->
           showPopupMenu(view)
        }

        return binding.root
    }

    private fun showPopupMenu(view1: View) {
        val popupMenu = PopupMenu(requireContext(), view1)
        popupMenu.menuInflater.inflate(R.menu.toolbarmenu, popupMenu.menu)

        popupMenu.setForceShowIcon(true)
        popupMenu.show()

    }

}