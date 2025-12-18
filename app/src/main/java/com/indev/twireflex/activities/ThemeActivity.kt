package com.indev.twireflex.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.indev.twireflex.activities.main.MainActivity
import com.indev.twireflex.model.Theme
import com.indev.twireflex.service.Settings

/**
 * Created by Sebastian Rask on 30-04-2016.
 */
open class ThemeActivity : AppCompatActivity() {
    private var theme: Theme? = null

    override fun onCreate(savedInstance: Bundle?) {
        loadTheme()
        super.onCreate(savedInstance)
    }

    public override fun onResume() {
        super.onResume()

        val currentTheme = Settings.theme
        if (currentTheme != theme) {
            recreate()
        }
    }

    private fun loadTheme() {
        this.theme = Settings.theme
        setTheme(theme!!.style)
    }

    override fun recreate() {
        if (this is MainActivity<*>) {
            this.recyclerView.scrollToPosition(0)
        }
        super.recreate()
    }
}
