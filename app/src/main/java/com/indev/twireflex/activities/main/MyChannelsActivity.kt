package com.indev.twireflex.activities.main

import com.indev.twireflex.R
import com.indev.twireflex.adapters.ChannelsAdapter
import com.indev.twireflex.adapters.MainActivityAdapter
import com.indev.twireflex.model.ChannelInfo
import com.indev.twireflex.service.TempStorage
import com.indev.twireflex.tasks.GetFollowsFromDB
import com.indev.twireflex.views.recyclerviews.AutoSpanRecyclerView
import com.indev.twireflex.views.recyclerviews.auto_span_behaviours.AutoSpanBehaviour
import com.indev.twireflex.views.recyclerviews.auto_span_behaviours.ChannelAutoSpanBehaviour

/**
 * Activity that shows the user's Twitch Follows.
 * If there are no follows in TempStorage when the activity is created, then the activity initiates an AsyncTask that connects to Twitch, that loads and adds the follows to this activity
 */
class MyChannelsActivity : LazyMainActivity<ChannelInfo>() {
    override fun constructAdapter(recyclerView: AutoSpanRecyclerView): MainActivityAdapter<ChannelInfo, *> {
        return ChannelsAdapter(mRecyclerView, baseContext, this)
    }

    override val activityIconRes: Int get() = R.drawable.ic_person

    override val activityTitleRes: Int get() = R.string.my_channels_activity_title

    override fun constructSpanBehaviour(): AutoSpanBehaviour {
        return ChannelAutoSpanBehaviour()
    }

    override fun addToAdapter(aObjectList: MutableList<ChannelInfo>) {
        mAdapter.addList(aObjectList)
    }

    override val visualElements: MutableList<ChannelInfo>
        get() {
            if (TempStorage.hasLoadedStreamers()) {
                return ArrayList(TempStorage.loadedStreamers)
            }

            val subscriptionsTask = GetFollowsFromDB(this)

            return ArrayList(subscriptionsTask.call().values)
        }
}
