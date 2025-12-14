package com.perflyst.twire.utils

import android.app.Activity
import android.graphics.Rect
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Manages flex mode detection and layout changes for foldable devices.
 *
 * Flex mode is activated when a foldable device (like Samsung Galaxy Fold) is
 * in a half-opened state with a horizontal hinge orientation (tabletop mode).
 * This creates an optimal viewing experience with video on the top half and
 * chat on the bottom half.
 */
class FlexModeManager(
    private val activity: Activity,
    private val onFlexModeChanged: (FlexModeState) -> Unit
) : DefaultLifecycleObserver {

    private var windowInfoTracker: WindowInfoTracker? = null
    private var collectionJob: Job? = null
    private var currentState: FlexModeState = FlexModeState.Flat

    /**
     * Represents the current flex mode state of the device.
     */
    sealed class FlexModeState {
        /** Device is fully open, fully closed, or not a foldable. */
        object Flat : FlexModeState()

        /**
         * Device is in flex mode (half-opened with horizontal hinge).
         * This is the "tabletop mode" ideal for video + chat layout.
         *
         * @param hingeBounds The rectangular bounds of the hinge/fold area
         * @param orientation The orientation of the fold (should be HORIZONTAL for tabletop mode)
         */
        data class FlexMode(
            val hingeBounds: Rect,
            val orientation: FoldingFeature.Orientation
        ) : FlexModeState() {
            /**
             * The Y position of the hinge, which determines where to split
             * the video (top) and chat (bottom) layout.
             */
            val hingeTop: Int get() = hingeBounds.top
            val hingeBottom: Int get() = hingeBounds.bottom
        }
    }

    /**
     * Check if the device is currently in flex mode.
     */
    val isFlexMode: Boolean
        get() = currentState is FlexModeState.FlexMode

    /**
     * Get the current flex mode state.
     */
    val state: FlexModeState
        get() = currentState

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        startWindowInfoCollection()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        stopWindowInfoCollection()
    }

    private fun startWindowInfoCollection() {
        windowInfoTracker = WindowInfoTracker.getOrCreate(activity)

        collectionJob = CoroutineScope(Dispatchers.Main).launch {
            windowInfoTracker?.windowLayoutInfo(activity)?.collect { layoutInfo ->
                processLayoutInfo(layoutInfo)
            }
        }
    }

    private fun stopWindowInfoCollection() {
        collectionJob?.cancel()
        collectionJob = null
    }

    private fun processLayoutInfo(layoutInfo: WindowLayoutInfo) {
        val foldingFeature = layoutInfo.displayFeatures
            .filterIsInstance<FoldingFeature>()
            .firstOrNull()

        val newState = when {
            // Check if device is in half-opened state with horizontal fold (tabletop mode)
            foldingFeature != null &&
            foldingFeature.state == FoldingFeature.State.HALF_OPENED &&
            foldingFeature.orientation == FoldingFeature.Orientation.HORIZONTAL -> {
                FlexModeState.FlexMode(
                    hingeBounds = foldingFeature.bounds,
                    orientation = foldingFeature.orientation
                )
            }
            else -> FlexModeState.Flat
        }

        // Only notify if state actually changed
        if (newState != currentState) {
            currentState = newState
            onFlexModeChanged(newState)
        }
    }

    companion object {
        private const val TAG = "FlexModeManager"
    }
}
