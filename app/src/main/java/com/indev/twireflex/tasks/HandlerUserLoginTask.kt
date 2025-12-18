package com.indev.twireflex.tasks

import com.indev.twireflex.TwireApplication
import com.indev.twireflex.activities.setup.LoginActivity
import com.indev.twireflex.service.Settings.generalTwitchDisplayName
import com.indev.twireflex.service.Settings.generalTwitchName
import com.indev.twireflex.service.Settings.generalTwitchUserBio
import com.indev.twireflex.service.Settings.generalTwitchUserCreatedDate
import com.indev.twireflex.service.Settings.generalTwitchUserID
import com.indev.twireflex.service.Settings.generalTwitchUserIsPartner
import com.indev.twireflex.service.Settings.generalTwitchUserLogo
import com.indev.twireflex.service.Settings.generalTwitchUserType
import com.indev.twireflex.service.Settings.generalTwitchUserUpdatedDate
import com.indev.twireflex.service.Settings.isLoggedIn
import com.indev.twireflex.utils.Execute
import java.lang.ref.WeakReference

/**
 * Created by SebastianRask on 03-11-2015.
 */
class HandlerUserLoginTask(mLoginActivity: LoginActivity?) : Runnable {
    private val mLoginActivity: WeakReference<LoginActivity?> =
        WeakReference<LoginActivity?>(mLoginActivity)

    override fun run() {
        // the User is fetched by the Bearer token
        isLoggedIn = true
        val users = TwireApplication.helix.getUsers(null, null, null).execute()
        val user = users.users[0]

        generalTwitchDisplayName = user.displayName
        generalTwitchName = user.login
        //mSettings.setGeneralTwitchUserEmail((String) mUserInfo[4]);
        generalTwitchUserCreatedDate = user.createdAt.toString()
        generalTwitchUserType = user.type
        generalTwitchUserIsPartner = user.broadcasterType == "partner"
        generalTwitchUserID = user.id

        if (user.description != null) generalTwitchUserBio = user.description

        if (user.profileImageUrl != null) generalTwitchUserLogo = user.profileImageUrl

        if (user.createdAt != null) generalTwitchUserUpdatedDate =
            user.createdAt.toString()

        // Now that we have the user, update the credential
        TwireApplication.updateCredential()

        Execute.ui { mLoginActivity.get()!!.handleLoginSuccess() }
    }
}
