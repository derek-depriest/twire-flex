package com.indev.twireflex.tasks

import com.indev.twireflex.TwireApplication
import com.indev.twireflex.utils.Constants
import java.util.concurrent.Callable

/**
 * Created by Sebastian Rask on 10-05-2016.
 */
class ValidateOauthTokenTask : Callable<String?> {
    override fun call(): String? {
        val response =
            TwireApplication.identityProvider.getAdditionalCredentialInformation(TwireApplication.credential)
                .orElse(null)
        if (response == null) return null

        // Validate that all required scopes are present
        if (!HashSet<String?>(response.scopes).containsAll(listOf(*Constants.TWITCH_SCOPES))) return null

        return response.getUserId()
    }
}
