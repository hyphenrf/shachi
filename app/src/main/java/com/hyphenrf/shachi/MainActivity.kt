package com.hyphenrf.shachi

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.LayoutTransition
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.hyphenrf.shachi.data.preference.ShachiPreference
import com.hyphenrf.shachi.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigationrail.NavigationRailView
// import com.google.firebase.crashlytics.ktx.crashlytics
// import com.google.firebase.ktx.Firebase
import com.google.modernstorage.permissions.RequestAccess
import com.google.modernstorage.permissions.StoragePermissions

class MainActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    lateinit var navController: NavController
    lateinit var sharedPreferences: SharedPreferences
    lateinit var binding: ActivityMainBinding
    var isShowNavigation: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        (binding.mainLayout as ViewGroup).layoutTransition?.enableTransitionType(LayoutTransition.CHANGING)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        setupNavController()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isShow = when (destination.id) {
                R.id.savedFragment -> true
                R.id.browseFragment -> true
                R.id.browseSavedFragment -> true
                R.id.moreFragment -> true
                R.id.favoriteFragment -> true
                else -> false
            }
            if (isShow) {
                showNavigation()
//                binding.sideNavigationRail?.show()
            } else {
                hideNavigation()
//                binding.sideNavigationRail?.hide()
            }
            Log.d("MainActivity", "addOnDestinationChangedListener $isShow")
        }
        setTheme()
        setSendCrashReports()

        val permissions = checkPermission()
        Log.d("MainActivity", "permission $permissions")
        if (permissions.isNotEmpty()) {
            requestPermission()
        }

        createNotificationChannel("download", "Download notification channel")
        createNotificationChannel("system",
            "System notification channel",
            NotificationManager.IMPORTANCE_DEFAULT)
    }

    private fun setupNavController() {
        val navFragment =
            supportFragmentManager.findFragmentById(R.id.navFragment) as NavHostFragment
        navController = navFragment.navController
        binding.bottomNavigationView?.setupWithNavController(navController)
        binding.sideNavigationRail?.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.popBackStack()
    }

    fun setTheme() {
        val theme = sharedPreferences.getString(ShachiPreference.KEY_THEME, "follow_system")

        val mode = when (theme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        Log.d("MainActivity", "$theme $mode")
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun setSendCrashReports() {
        // val isSend = sharedPreferences.getBoolean(ShachiPreference.KEY_SEND_CRASH_REPORTS, true)
        // Firebase.crashlytics.setCrashlyticsCollectionEnabled(isSend)
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference,
    ): Boolean = when (pref.key) {
        ShachiPreference.KEY_SETTINGS -> {
            navController.navigate(R.id.action_more_to_settings)
            true
        }
        ShachiPreference.KEY_SERVERS -> {
            navController.navigate(R.id.action_global_to_servers)
            true
        }
        ShachiPreference.KEY_BLACKLISTED_TAGS -> {
            navController.navigate(R.id.action_global_to_blacklisted_tags)
            true
        }
        ShachiPreference.KEY_OSS_NOTICES -> {
            navController.navigate(R.id.action_more_to_oss)
            true
        }
        ShachiPreference.KEY_ABOUT -> {
            navController.navigate(R.id.action_more_to_about)
            true
        }
        else -> false
    }

    fun showNavigation(callback: (() -> Unit)? = null) {
        binding.bottomNavigationView?.show(callback)
        binding.sideNavigationRail?.show()
    }

    fun hideNavigation(callback: (() -> Unit)? = null) {
        binding.bottomNavigationView?.hide(callback)
        binding.sideNavigationRail?.hide()
    }

    private fun BottomNavigationView.hide(callback: (() -> Unit)? = null) {
        if (!isShowNavigation) return
        isShowNavigation = false
        val constraint = binding.mainLayout
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.mainLayout)
        constraintSet.connect(R.id.navFragment,
            ConstraintSet.BOTTOM,
            R.id.mainLayout,
            ConstraintSet.BOTTOM)
        constraintSet.applyTo(constraint)
        animate()
            .translationY(height.toFloat())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    if (callback != null) callback()
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    visibility = View.GONE
                }
            })
    }

    private fun NavigationRailView.hide() {
        if (!isShowNavigation) return
        isShowNavigation = false
        isGone = true
    }

    private fun BottomNavigationView.show(callback: (() -> Unit)? = null) {
        if (isShowNavigation) return
        isShowNavigation = true
        animate().translationY(0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    val constraint = binding.mainLayout
                    val constraintSet = ConstraintSet()
                    constraintSet.clone(constraint)
                    constraintSet.connect(R.id.navFragment,
                        ConstraintSet.BOTTOM,
                        R.id.bottomNavigationView,
                        ConstraintSet.TOP)
                    constraintSet.applyTo(constraint)
                    if (callback != null) callback()
                }
            })
    }

    private fun NavigationRailView.show() {
        if (isShowNavigation) return
        isShowNavigation = true
        isVisible = true
    }

    private fun checkPermission(): List<String> {
        return StoragePermissions.getPermissions(
            action = StoragePermissions.Action.READ_AND_WRITE,
            types = listOf(StoragePermissions.FileType.Image, StoragePermissions.FileType.Video),
            createdBy = StoragePermissions.CreatedBy.Self
        )
    }

    private val requestAccess = registerForActivityResult(RequestAccess()) { hasAccess ->
        if (hasAccess) {
            Log.d("MainActivity", "$hasAccess")
        }
    }

    private fun requestPermission() {
        Log.d("MainActivity", "request permission")
        requestAccess.launch(RequestAccess.Args(
            action = StoragePermissions.Action.READ_AND_WRITE,
            types = listOf(StoragePermissions.FileType.Image, StoragePermissions.FileType.Video),
            createdBy = StoragePermissions.CreatedBy.Self
        ))
    }

    private fun createNotificationChannel(
        name: String,
        descriptionText: String,
        importance: Int = NotificationManager.IMPORTANCE_LOW,
    ) {
        val channel = NotificationChannel(name, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}