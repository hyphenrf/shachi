package com.hyphenrf.shachi.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hyphenrf.shachi.MainActivity
import com.hyphenrf.shachi.R
import com.hyphenrf.shachi.databinding.AboutFragmentBinding
import com.hyphenrf.shachi.databinding.MoreFragmentBinding

class AboutFragment : Fragment() {
    private lateinit var binding: AboutFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = AboutFragmentBinding.inflate(inflater, container, false)

        (activity as MainActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, AboutSettingsFragment()).commit()
        return binding.root
    }
}