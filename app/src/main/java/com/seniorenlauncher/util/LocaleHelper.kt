package com.seniorenlauncher.util
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    fun setLocale(ctx: Context, lang: String): Context {
        val locale = Locale(lang); Locale.setDefault(locale)
        val config = Configuration(ctx.resources.configuration); config.setLocale(locale)
        return ctx.createConfigurationContext(config)
    }
    val SUPPORTED = mapOf("nl" to "Nederlands","fr" to "Français","de" to "Deutsch","en" to "English","tr" to "Türkçe","ar" to "العربية")
}
