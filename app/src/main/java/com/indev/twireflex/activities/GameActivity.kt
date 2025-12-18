package com.indev.twireflex.activities

import com.indev.twireflex.R
import com.indev.twireflex.TwireApplication
import com.indev.twireflex.activities.main.LazyMainActivity
import com.indev.twireflex.adapters.MainActivityAdapter
import com.indev.twireflex.adapters.StreamsAdapter
import com.indev.twireflex.misc.Utils
import com.indev.twireflex.model.Game
import com.indev.twireflex.model.StreamInfo
import com.indev.twireflex.service.Settings.generalFilterTopStreamsByLanguage
import com.indev.twireflex.views.recyclerviews.AutoSpanRecyclerView
import com.indev.twireflex.views.recyclerviews.auto_span_behaviours.AutoSpanBehaviour
import com.indev.twireflex.views.recyclerviews.auto_span_behaviours.StreamAutoSpanBehaviour

class GameActivity : LazyMainActivity<StreamInfo>() {
    private var game: Game? = null

    override fun constructAdapter(recyclerView: AutoSpanRecyclerView): MainActivityAdapter<StreamInfo, *> {
        return StreamsAdapter(recyclerView, this)
    }

    public override fun customizeActivity() {
        val intent = getIntent()
        game = intent.getParcelableExtra(getString(R.string.game_intent_key))
        checkNotNull(game)
        mTitleView.text = game!!.gameTitle
    }

    override val activityIconRes: Int get() = R.drawable.ic_sports_esports

    override val activityTitleRes: Int get() = R.string.my_streams_activity_title

    override fun constructSpanBehaviour(): AutoSpanBehaviour {
        return StreamAutoSpanBehaviour()
    }

    override fun addToAdapter(streamsToAdd: MutableList<StreamInfo>) {
        mAdapter.addList(streamsToAdd)
    }

    override val visualElements: MutableList<StreamInfo>
        get() {
            val languageFilter =
                if (generalFilterTopStreamsByLanguage) Utils.systemLanguage else null
            val response = TwireApplication.helix.getStreams(
                null,
                cursor,
                null,
                limit,
                listOf(game!!.gameId),
                listOf(languageFilter),
                null,
                null
            ).execute()
            cursor = response.pagination.cursor
            return response.streams.map(::StreamInfo).toMutableList()
        }
}
