package com.github.lure0xaos.jrpycg.services

import com.github.lure0xaos.util.pref.set
import java.util.*
import java.util.prefs.Preferences

class LocaleHolder(private val preferences: Preferences) {
    var locale: Locale
        get() = preferences[PREF_LOCALE_UI, Locale.getDefault().language].let { Locale.forLanguageTag(it) }
        set(value) {
            preferences[PREF_LOCALE_UI] = value.language
            ResourceBundle.clearCache()
        }

    companion object {
        private const val PREF_LOCALE_UI = "localeUI"
    }
}
