package com.indev.twireflex.views.recyclerviews.auto_span_behaviours

import android.content.Context
import com.indev.twireflex.R

/**
 * Created by Sebastian Rask on 09-05-2017.
 */
class EmoteAutoSpanBehaviour : AutoSpanBehaviour {
    override val elementSizeName: String get() = ""

    override fun getElementWidth(context: Context): Int {
        return context.resources.getDimension(R.dimen.chat_grid_emote_size).toInt()
    }
}
