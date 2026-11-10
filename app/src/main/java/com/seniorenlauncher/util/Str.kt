package com.seniorenlauncher.util

import android.content.Context
import androidx.annotation.StringRes
import com.seniorenlauncher.LauncherApp
import java.util.Locale

/** Globale toegang tot gelokaliseerde strings, bruikbaar in composables, services en receivers. */
object Str {
    @Volatile private var localized: Context? = null
    @Volatile private var lang: String? = null

    private fun ctx(): Context {
        val app = LauncherApp.instance
        val current = LocaleHelper.saved(app)
        if (localized == null || lang != current) refresh(app)
        return localized!!
    }

    fun refresh(app: Context) {
        val l = LocaleHelper.saved(app)
        lang = l
        localized = LocaleHelper.setLocale(app.applicationContext, l)
    }

    fun get(@StringRes id: Int, vararg args: Any?): String {
        val c = ctx()
        return if (args.isEmpty()) c.getString(id) else c.getString(id, *args)
    }

    fun locale(): Locale = Locale.forLanguageTag(LocaleHelper.ttsTag(LocaleHelper.saved(LauncherApp.instance)))
}

fun i18n(@StringRes id: Int, vararg args: Any?): String = Str.get(id, *args)
