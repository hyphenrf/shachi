package com.hyphenrf.shachi.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.hyphenrf.shachi.R
import com.hyphenrf.shachi.data.preference.ShachiPreference

class AboutSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about_preferences, rootKey)
        findPreference<Preference>(ShachiPreference.KEY_APP_VERSION)?.apply {
            val version = @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            summary = "v${version} (GPL-3.0)"
            setOnPreferenceClickListener {
                AlertDialog.Builder(context)
                    .setMessage(getString(R.string.license_notice).trimIndent())
                    .setPositiveButton(R.string.license_agree, null)
                    .show()
                true
            }
        }

        findPreference<Preference>(ShachiPreference.KEY_GITHUB)?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/hyphenrf/shachi"))
            startActivity(intent)
            true
        }
    }
}
