package com.indev.twireflex.fragments

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.indev.twireflex.BuildConfig
import com.indev.twireflex.R
import com.indev.twireflex.activities.settings.SettingsTwitchChatActivity
import com.indev.twireflex.service.DialogService
import com.indev.twireflex.service.Settings.lastVersionCode
import com.rey.material.widget.Button

/**
 * Dialog shown on first launch to welcome users to Twire Flex
 * and explain the Flex Mode feature for foldable devices
 */
class WelcomeToFlexDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity: Activity = requireActivity()

        val dialog = DialogService.getBaseThemedDialog(activity)
            .title(R.string.welcome_to_flex_title)
            .customView(R.layout.dialog_welcome_flex, false)
            .build()

        checkNotNull(dialog.customView)
        val customView: View = dialog.customView!!

        val gotItButton = customView.findViewById<Button>(R.id.got_it_button)
        gotItButton.setOnClickListener {
            dialog.dismiss()
        }

        val flexSettingsButton = customView.findViewById<Button>(R.id.flex_settings_button)
        flexSettingsButton.setOnClickListener {
            // Open Flex Mode settings
            val intent = Intent(activity, SettingsTwitchChatActivity::class.java)
            activity.startActivity(intent)
            dialog.dismiss()
        }

        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        // Mark as shown by updating lastVersionCode
        lastVersionCode = BuildConfig.VERSION_CODE
    }
}
