package com.indev.twireflex.activities.settings

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.CheckedTextView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.indev.twireflex.R
import com.indev.twireflex.activities.ThemeActivity
import com.indev.twireflex.databinding.ActivitySettingsTwitchChatBinding
import com.indev.twireflex.misc.Utils
import com.indev.twireflex.service.DialogService
import com.indev.twireflex.service.Settings.chatAccountConnect
import com.indev.twireflex.service.Settings.chatEmoteBTTV
import com.indev.twireflex.service.Settings.chatEmoteFFZ
import com.indev.twireflex.service.Settings.chatEmoteSEVENTV
import com.indev.twireflex.service.Settings.chatEnableSSL
import com.indev.twireflex.service.Settings.chatLandscapeWidth
import com.indev.twireflex.service.Settings.emoteSize
import com.indev.twireflex.service.Settings.flexModeEnabled
import com.indev.twireflex.service.Settings.isChatInLandscapeEnabled
import com.indev.twireflex.service.Settings.isChatLandscapeSwipeable
import com.indev.twireflex.service.Settings.messageSize
import com.rey.material.widget.Slider

class SettingsTwitchChatActivity : ThemeActivity() {
    private lateinit var emoteSizeSummary: TextView
    private lateinit var messageSizeSummary: TextView
    private lateinit var chatLandscapeWidthSummary: TextView
    private lateinit var chatLandscapeToggleSummary: TextView
    private lateinit var chatLandscapeSwipeToShowSummary: TextView
    private lateinit var chatEnableSslSummary: TextView
    private lateinit var chatEnableAccountConnectSummary: TextView
    private lateinit var chatEnableEmoteBBTVSummary: TextView
    private lateinit var chatEnableEmoteFFZSummary: TextView
    private lateinit var chatEnableEmoteSeventvSummary: TextView
    private lateinit var flexModeEnableSummary: TextView
    private lateinit var chatLandscapeToggle: CheckedTextView
    private lateinit var chatSwipeToShowToggle: CheckedTextView
    private lateinit var chatEnableSsl: CheckedTextView
    private lateinit var chatEnableAccountConnect: CheckedTextView
    private lateinit var chatEnableEmoteBBTV: CheckedTextView
    private lateinit var chatEnableEmoteFFZ: CheckedTextView
    private lateinit var chatEnableEmoteSeventv: CheckedTextView
    private lateinit var flexModeEnableToggle: CheckedTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsTwitchChatBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        val toolbar = findViewById<Toolbar?>(R.id.settings_player_toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        emoteSizeSummary = findViewById(R.id.chat_emote_size_summary)
        messageSizeSummary = findViewById(R.id.message_size_summary)
        chatLandscapeWidthSummary = findViewById(R.id.chat_landscape_summary)
        chatLandscapeToggleSummary = findViewById(R.id.chat_landscape_enable_summary)
        chatLandscapeSwipeToShowSummary = findViewById(R.id.chat_landscape_swipe_summary)
        chatEnableSslSummary = findViewById(R.id.chat_enable_ssl_summary)
        chatEnableEmoteBBTVSummary = findViewById(R.id.chat_enable_emote_bttv_summary)
        chatEnableEmoteFFZSummary = findViewById(R.id.chat_enable_emote_ffz_summary)
        chatEnableEmoteSeventvSummary =
            findViewById(R.id.chat_enable_emote_seventv_summary)
        chatEnableAccountConnectSummary =
            findViewById(R.id.chat_enable_account_connect_summary)
        flexModeEnableSummary = findViewById(R.id.flex_mode_enable_summary)

        chatLandscapeToggle = findViewById(R.id.chat_landscape_enable_title)
        chatSwipeToShowToggle = findViewById(R.id.chat_landscape_swipe_title)
        chatEnableSsl = findViewById(R.id.chat_enable_ssl)
        chatEnableEmoteBBTV = findViewById(R.id.chat_enable_emote_bttv)
        chatEnableEmoteFFZ = findViewById(R.id.chat_enable_emote_ffz)
        chatEnableEmoteSeventv = findViewById(R.id.chat_enable_emote_seventv)
        chatEnableAccountConnect =
            findViewById(R.id.chat_enable_account_connect)
        flexModeEnableToggle = findViewById(R.id.flex_mode_enable_title)

        updateSummaries()

        binding.emoteSizeButton.setOnClickListener { view: View? ->
            this.onClickEmoteSize()
        }
        binding.messageSizeButton.setOnClickListener { view: View? ->
            this.onClickMessageSize()
        }
        binding.landscapeEnableButton.setOnClickListener { view: View? ->
            this.onClickChatLandscapeEnable()
        }
        binding.landscapeSwipeButton.setOnClickListener { view: View? ->
            this.onClickChatLandscapeSwipeable()
        }
        binding.landscapeWidthButton.setOnClickListener { view: View? ->
            this.onClickChatLandScapeWidth()
        }
        binding.enableSslButton.setOnClickListener { view: View? ->
            this.onClickChatEnableSSL()
        }
        binding.accountConnectButton.setOnClickListener { view: View? ->
            this.onClickChatAccountConnect()
        }
        binding.emoteBttvButton.setOnClickListener { view: View? ->
            this.onClickChatEmoteBTTV()
        }
        binding.emoteFfzButton.setOnClickListener { view: View? ->
            this.onClickChatEmoteFFZ()
        }
        binding.emoteSeventvButton.setOnClickListener { view: View? ->
            this.onClickChatEmoteSEVENTV()
        }
        binding.flexModeEnableButton.setOnClickListener { view: View? ->
            this.onClickFlexModeEnable()
        }
    }

    private fun updateSummary(checkView: CheckedTextView, summary: TextView, isEnabled: Boolean) {
        checkView.isChecked = isEnabled
        summary.setText(if (isEnabled) R.string.enabled else R.string.disabled)
    }

    private fun updateSummaries() {
        val sizes = getResources().getStringArray(R.array.ChatSize)
        emoteSizeSummary.text = sizes[emoteSize - 1]
        messageSizeSummary.text = sizes[messageSize - 1]
        Utils.setPercent(chatLandscapeWidthSummary, (chatLandscapeWidth / 100f).toDouble())

        // Chat enabled in landscape
        updateSummary(chatLandscapeToggle, chatLandscapeToggleSummary, isChatInLandscapeEnabled)
        // Chat showable by swiping
        updateSummary(
            chatSwipeToShowToggle,
            chatLandscapeSwipeToShowSummary,
            isChatLandscapeSwipeable
        )
        // Chat SSL Enabled
        updateSummary(chatEnableSsl, chatEnableSslSummary, chatEnableSSL)
        // Update Chat Emote Stuff
        updateSummary(chatEnableEmoteBBTV, chatEnableEmoteBBTVSummary, chatEmoteBTTV)
        updateSummary(chatEnableEmoteFFZ, chatEnableEmoteFFZSummary, chatEmoteFFZ)
        updateSummary(
            chatEnableEmoteSeventv,
            chatEnableEmoteSeventvSummary,
            chatEmoteSEVENTV
        )
        // Chat enable Login with Account
        updateSummary(
            chatEnableAccountConnect,
            chatEnableAccountConnectSummary,
            chatAccountConnect
        )
        // Flex Mode enable
        updateSummary(
            flexModeEnableToggle,
            flexModeEnableSummary,
            flexModeEnabled
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.overridePendingTransition(R.anim.fade_in_semi_anim, R.anim.slide_out_right_anim)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    fun onClickEmoteSize() {
        val dialog = DialogService.getChooseChatSizeDialog(
            this,
            R.string.chat_emote_size,
            R.array.ChatSize,
            emoteSize
        ) { dialog1: MaterialDialog?, itemView: View?, which: Int, text: CharSequence? ->
            emoteSize = which + 1
            updateSummaries()
            true
        }
        dialog.show()
    }

    fun onClickMessageSize() {
        val dialog = DialogService.getChooseChatSizeDialog(
            this,
            R.string.chat_message_size,
            R.array.ChatSize,
            messageSize
        ) { dialog1: MaterialDialog?, itemView: View?, which: Int, text: CharSequence? ->
            messageSize = which + 1
            updateSummaries()
            true
        }
        dialog.show()
    }

    fun onClickChatLandscapeEnable() {
        isChatInLandscapeEnabled = !isChatInLandscapeEnabled
        updateSummaries()
    }

    fun onClickChatLandscapeSwipeable() {
        isChatLandscapeSwipeable = !isChatLandscapeSwipeable
        updateSummaries()
    }

    fun onClickChatEnableSSL() {
        chatEnableSSL = !chatEnableSSL
        updateSummaries()
    }


    fun onClickChatEmoteBTTV() {
        chatEmoteBTTV = !chatEmoteBTTV
        updateSummaries()
    }

    fun onClickChatEmoteFFZ() {
        chatEmoteFFZ = !chatEmoteFFZ
        updateSummaries()
    }

    fun onClickChatEmoteSEVENTV() {
        chatEmoteSEVENTV = !chatEmoteSEVENTV
        updateSummaries()
    }

    fun onClickChatAccountConnect() {
        chatAccountConnect = !chatAccountConnect
        updateSummaries()
    }

    fun onClickFlexModeEnable() {
        flexModeEnabled = !flexModeEnabled
        updateSummaries()
    }

    fun onClickChatLandScapeWidth() {
        val landscapeWidth = chatLandscapeWidth

        DialogService.getSliderDialog(
            this,
            { dialog: MaterialDialog?, which: DialogAction? ->
                chatLandscapeWidth = landscapeWidth
                updateSummaries()
            },
            { view: Slider?, fromUser: Boolean, oldPos: Float, newPos: Float, oldValue: Int, newValue: Int ->
                chatLandscapeWidth = newValue
                updateSummaries()
            },
            landscapeWidth,
            10,
            60,
            getString(R.string.chat_landscape_width_dialog)
        ).show()
    }
}
