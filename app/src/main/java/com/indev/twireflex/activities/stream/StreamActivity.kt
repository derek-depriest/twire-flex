package com.indev.twireflex.activities.stream

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.Fade
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.indev.twireflex.R
import com.indev.twireflex.activities.ThemeActivity
import com.indev.twireflex.fragments.ChatFragment
import com.indev.twireflex.fragments.ChatFragment.Companion.getInstance
import com.indev.twireflex.fragments.StreamFragment
import com.indev.twireflex.fragments.StreamFragment.Companion.getScreenRect
import com.indev.twireflex.fragments.StreamFragment.Companion.newInstance
import com.indev.twireflex.fragments.StreamFragment.StreamFragmentListener
import com.indev.twireflex.service.Settings.chatLandscapeWidth
import com.indev.twireflex.service.Settings.flexModeEnabled
import com.indev.twireflex.utils.FlexModeManager
import timber.log.Timber

abstract class StreamActivity : ThemeActivity(), StreamFragmentListener {
    var mStreamFragment: StreamFragment? = null
    var mChatFragment: ChatFragment? = null
    private var mBackstackLost = false
    private var onStopCalled = false
    private var initialOrientation = 0

    // Flex mode support for foldable devices
    private lateinit var flexModeManager: FlexModeManager
    private var currentFlexModeState: FlexModeManager.FlexModeState = FlexModeManager.FlexModeState.Flat
    private var isInFlexMode = false

    protected abstract val layoutResource: Int

    protected abstract val videoContainerResource: Int

    protected abstract val streamArguments: Bundle?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this.layoutResource)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        initialOrientation = getResources().configuration.orientation

        // Initialize flex mode manager for foldable device support
        initFlexModeManager()

        if (savedInstanceState == null) {
            val fm = supportFragmentManager

            window.setEnterTransition(constructTransitions())
            window.setReturnTransition(constructTransitions())

            // If the Fragment is non-null, then it is currently being
            // retained across a configuration change.
            if (mChatFragment == null) {
                mChatFragment = getInstance(this.streamArguments)
                fm.beginTransaction().replace(R.id.chat_fragment, mChatFragment!!).commit()
            }

            if (mStreamFragment == null) {
                mStreamFragment = newInstance(this.streamArguments)
                fm.beginTransaction().replace(
                    this.videoContainerResource,
                    mStreamFragment!!,
                    getString(R.string.stream_fragment_tag)
                ).commit()
            }
        }

        updateOrientation()
    }

    /**
     * Initialize the flex mode manager for foldable device support.
     * This detects when the device enters "tabletop mode" (half-folded with horizontal hinge)
     * and adjusts the layout to show video on top and chat on bottom.
     */
    private fun initFlexModeManager() {
        flexModeManager = FlexModeManager(this) { state ->
            currentFlexModeState = state
            if (flexModeEnabled) {
                onFlexModeStateChanged(state)
            }
        }
        lifecycle.addObserver(flexModeManager)
    }

    /**
     * Handle flex mode state changes.
     * When entering flex mode, switch to a split layout with video on top and chat on bottom.
     */
    private fun onFlexModeStateChanged(state: FlexModeManager.FlexModeState) {
        Timber.d("Flex mode state changed: $state")

        when (state) {
            is FlexModeManager.FlexModeState.FlexMode -> {
                if (!isInFlexMode) {
                    isInFlexMode = true
                    enableFlexModeLayout(state)
                }
            }
            is FlexModeManager.FlexModeState.Flat -> {
                if (isInFlexMode) {
                    isInFlexMode = false
                    disableFlexModeLayout()
                }
            }
        }
    }

    /**
     * Enable flex mode layout - video on top half, chat on bottom half.
     * Uses smooth transitions for a polished user experience.
     */
    private fun enableFlexModeLayout(state: FlexModeManager.FlexModeState.FlexMode) {
        Timber.d("Enabling flex mode layout. Hinge at Y: ${state.hingeTop}")

        val mainContent = findViewById<ViewGroup>(R.id.main_content) ?: return
        val videoContainer = findViewById<View>(this.videoContainerResource) ?: return
        val chatContainer = findViewById<View>(R.id.chat_fragment) ?: return

        // Animate the layout transition
        val transition = AutoTransition().apply {
            duration = 300
            interpolator = DecelerateInterpolator()
        }
        TransitionManager.beginDelayedTransition(mainContent, transition)

        // Set video container to fill from top to hinge position
        val videoParams = videoContainer.layoutParams as RelativeLayout.LayoutParams
        videoParams.height = state.hingeTop
        videoParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        videoParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        videoContainer.layoutParams = videoParams

        // Set chat container to fill from hinge to bottom
        val chatParams = chatContainer.layoutParams as RelativeLayout.LayoutParams
        chatParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        chatParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        chatParams.addRule(RelativeLayout.BELOW, this.videoContainerResource)
        chatParams.removeRule(RelativeLayout.ALIGN_PARENT_END)
        chatParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        chatContainer.layoutParams = chatParams

        // Hide the landscape chat wrapper since we're using flex mode layout
        findViewById<View>(R.id.chat_landscape_fragment)?.visibility = View.GONE
        findViewById<View>(R.id.chat_placement_wrapper)?.visibility = View.GONE

        // Notify StreamFragment to use centered video mode for Flex Mode
        mStreamFragment?.applyFlexModeVideoSettings()

        // Notify ChatFragment to maximize chat (hide emote picker by default)
        mChatFragment?.maximizeChatForFlexMode()
    }

    /**
     * Disable flex mode layout and return to normal orientation-based layout.
     */
    private fun disableFlexModeLayout() {
        Timber.d("Disabling flex mode layout")

        // Restore visibility of layout wrappers
        findViewById<View>(R.id.chat_landscape_fragment)?.visibility = View.VISIBLE
        findViewById<View>(R.id.chat_placement_wrapper)?.visibility = View.VISIBLE

        // Notify StreamFragment to revert video settings
        mStreamFragment?.revertFlexModeVideoSettings()

        // Revert to standard orientation-based layout
        updateOrientation()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateOrientation()
    }

    protected fun resetStream() {
        val fm = supportFragmentManager
        mStreamFragment = newInstance(this.streamArguments)
        fm.beginTransaction().replace(this.videoContainerResource, mStreamFragment!!).commit()
    }

    public override fun onPause() {
        super.onPause()
    }

    override fun onBackPressed() {
        if (mChatFragment == null || !mChatFragment!!.notifyBackPressed()) {
            return
        }

        // Eww >(
        if (mStreamFragment != null) {
            val isCurrentlyLandscape =
                getResources().configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            val wasInitiallyLandscape = initialOrientation == Configuration.ORIENTATION_LANDSCAPE
            if (isCurrentlyLandscape && !wasInitiallyLandscape) {
                mStreamFragment!!.toggleFullscreen()
            } else if (mStreamFragment!!.chatOnlyViewVisible) {
                this.finish()
                this.overrideTransition()
            } else {
                super.onBackPressed()
                try {
                    mStreamFragment!!.backPressed()
                } catch (e: NullPointerException) {
                    Timber.e(e)
                }
                this.overrideTransition()
            }
        } else {
            super.onBackPressed()
            this.overrideTransition()
        }
    }

    @RequiresApi(24)
    public override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return
        }

        if (mStreamFragment!!.playWhenReady && applicationContext.packageManager
                .hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        ) {
            enterPictureInPictureMode()
        }
    }

    private fun constructTransitions(): TransitionSet {
        val slideTargets =
            intArrayOf(R.id.ChatRecyclerView, R.id.chat_input, R.id.chat_input_divider)

        val slideTransition: Transition = Slide(Gravity.BOTTOM)
        val fadeTransition: Transition = Fade()

        for (slideTarget in slideTargets) {
            slideTransition.addTarget(slideTarget)
            fadeTransition.excludeTarget(slideTarget, true)
        }

        val set = TransitionSet()
        set.addTransition(slideTransition)
        set.addTransition(fadeTransition)
        return set
    }

    private fun overrideTransition() {
        this.overridePendingTransition(R.anim.fade_in_semi_anim, R.anim.slide_out_bottom_anim)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_stream, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { // Call the super method as we also want the user to go all the way back to last mActivity if the user is in full screen mode
            if (mStreamFragment != null) {
                if (!mStreamFragment!!.isVideoInterfaceShowing) {
                    return false
                }

                if (mStreamFragment!!.chatOnlyViewVisible) {
                    finish()
                } else {
                    super.onBackPressed()
                    mStreamFragment!!.backPressed()
                }
                overrideTransition()
            }

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is StreamFragment) {
            val streamFragment = fragment
            streamFragment.streamFragmentCallback = this
        }

        if (mChatFragment == null && fragment is ChatFragment) mChatFragment = fragment

        if (mStreamFragment == null && fragment is StreamFragment) mStreamFragment = fragment
    }

    override fun onSeek() {
        mChatFragment!!.clearMessages()
    }

    override fun refreshLayout() {
        updateOrientation()
    }

    val mainContentLayout: View?
        get() = findViewById(R.id.main_content)

    fun updateOrientation() {
        // If we're in flex mode, don't apply standard orientation changes
        if (isInFlexMode && flexModeEnabled) {
            Timber.d("Skipping orientation update - in flex mode")
            return
        }

        val landscape =
            getResources().configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val chat = findViewById<View>(R.id.chat_fragment)
        if (landscape) {
            val lp =
                findViewById<View?>(R.id.chat_landscape_fragment)?.layoutParams as RelativeLayout.LayoutParams
            lp.width = (getScreenRect(this).height() * (chatLandscapeWidth / 100.0)).toInt()
            Timber.d("TARGET WIDTH: %s", lp.width)
            chat.setLayoutParams(lp)
        } else {
            chat.setLayoutParams(findViewById<View?>(R.id.chat_placement_wrapper)?.layoutParams)
        }

        val layoutParams = findViewById<View?>(this.videoContainerResource)?.layoutParams
        layoutParams?.height =
            if (landscape) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
    }

    public override fun onStop() {
        super.onStop()
        onStopCalled = true
    }

    override fun onResume() {
        super.onResume()
        onStopCalled = false
    }

    override fun onPictureInPictureModeChanged(enabled: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(enabled, newConfig)
        mBackstackLost = mBackstackLost or enabled

        if (!enabled && onStopCalled) {
            finish()
        }
    }

    override fun finish() {
        if (mBackstackLost) {
            navToLauncherTask(applicationContext)
            finishAndRemoveTask()
        } else {
            super.finish()
        }
    }

    fun navToLauncherTask(appContext: Context) {
        val activityManager = ContextCompat.getSystemService(
            appContext,
            ActivityManager::class.java
        )
        // iterate app tasks available and navigate to launcher task (browse task)
        if (activityManager != null) {
            val appTasks = activityManager.getAppTasks()
            for (task in appTasks) {
                val baseIntent = task.taskInfo.baseIntent
                val categories = baseIntent.categories
                if (categories != null && categories.contains(Intent.CATEGORY_LAUNCHER)) {
                    task.moveToFront()
                    return
                }
            }
        }
    }
}
