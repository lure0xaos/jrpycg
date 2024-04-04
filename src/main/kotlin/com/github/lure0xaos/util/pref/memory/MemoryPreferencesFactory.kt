package com.github.lure0xaos.util.pref.memory

import java.util.prefs.Preferences
import java.util.prefs.PreferencesFactory

class MemoryPreferencesFactory : PreferencesFactory {
    override fun systemRoot(): Preferences = MemoryPreferences(null, "")

    override fun userRoot(): Preferences = MemoryPreferences(null, "")

    init {
        System.setProperty("java.util.prefs.PreferencesFactory", MemoryPreferencesFactory::class.qualifiedName!!)
    }
}
