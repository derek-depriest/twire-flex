package com.indev.twireflex.tasks

import com.indev.twireflex.TwireApplication
import java.util.concurrent.Callable

/**
 * Created by Sebastian Rask on 17-09-2016.
 */
class GetStreamViewersTask(private val streamerUserId: String) : Callable<Int?> {
    override fun call(): Int? {
        val streams = TwireApplication.helix.getStreams(
            null,
            null,
            null,
            1,
            null,
            null,
            listOf(this.streamerUserId),
            null
        ).execute()
        return if (streams.streams.isEmpty()) null else streams.streams[0].viewerCount
    }
}
