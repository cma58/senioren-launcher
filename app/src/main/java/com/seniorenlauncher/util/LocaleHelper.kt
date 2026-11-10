package com.seniorenlauncher.util
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    private const val PREFS = "locale_prefs"
    private const val KEY = "language"
    const val DEFAULT = "ru"

    fun setLocale(ctx: Context, lang: String): Context {
        val locale = Locale(lang); Locale.setDefault(locale)
        val config = Configuration(ctx.resources.configuration); config.setLocale(locale)
        return ctx.createConfigurationContext(config)
    }

    /** Taal die synchroon beschikbaar is (SharedPreferences), zodat ook services/receivers de juiste taal gebruiken. */
    fun saved(ctx: Context): String =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, DEFAULT) ?: DEFAULT

    fun save(ctx: Context, lang: String) {
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, lang).putBoolean(CHOSEN, true).apply()
    }

    private const val CHOSEN = "chosen"

    /** True zodra de gebruiker (of een bestaande installatie) een taal heeft vastgelegd. */
    fun isChosen(ctx: Context): Boolean =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(CHOSEN, false)

    /** BCP-47 tag voor TextToSpeech en dergelijke. */
    fun ttsTag(lang: String): String = when (lang) {
        "ru" -> "ru-RU"; "en" -> "en-GB"; "fr" -> "fr-FR"; "de" -> "de-DE"; "tr" -> "tr-TR"; "ar" -> "ar"
        else -> "nl-NL"
    }

    val SUPPORTED = mapOf("nl" to "Nederlands","fr" to "Français","de" to "Deutsch","en" to "English","tr" to "Türkçe","ar" to "العربية","ru" to "Русский")
}
