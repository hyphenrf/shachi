package com.hyphenrf.shachi.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hyphenrf.shachi.MainActivity
import com.hyphenrf.shachi.R
import com.hyphenrf.shachi.databinding.MoreFragmentBinding
import com.google.android.material.shape.MaterialShapeDrawable

class MoreFragment : Fragment() {
    private lateinit var binding: MoreFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = MoreFragmentBinding.inflate(inflater, container, false)

        binding.moreAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())

        (activity as MainActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.moreFrameLayout, MoreSettingsFragment()).commit()
        return binding.root
    }
}