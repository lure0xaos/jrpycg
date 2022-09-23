package com.github.lure0xaos.jrpycg.services

import java.util.Locale
import java.util.prefs.Preferences

class LocaleHolder(private val preferences: Preferences) {
    var locale: Locale
        get() = preferences.get(PREF_LOCALE_UI, Locale.getDefault().language).let { Locale(it) }
        set(value) = preferences.put(PREF_LOCALE_UI, value.language)

    companion object {
        private const val PREF_LOCALE_UI = "localeUI"
    }
}
