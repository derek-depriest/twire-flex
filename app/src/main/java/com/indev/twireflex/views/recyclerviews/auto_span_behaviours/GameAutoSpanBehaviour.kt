package com.indev.twireflex.views.recyclerviews.auto_span_behaviours

import android.content.Context
import com.indev.twireflex.R
import com.indev.twireflex.service.Settings.appearanceGameSize

/**
 * Created by Sebastian Rask on 09-05-2017.
 */
class GameAutoSpanBehaviour : AutoSpanBehaviour {
    override val elementSizeName: String get() = appearanceGameSize

    override fun getElementWidth(context: Context): Int {
        return context.getResources().getDimension(R.dimen.game_card_width)
            .toInt() + context.getResources().getDimension(R.dimen.game_card_margin).toInt()
    }
}
