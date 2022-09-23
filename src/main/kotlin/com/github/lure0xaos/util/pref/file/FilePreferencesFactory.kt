package com.github.lure0xaos.util.pref.file

import com.github.lure0xaos.util.privileged
import java.util.prefs.Preferences
import java.util.prefs.PreferencesFactory

class FilePreferencesFactory : PreferencesFactory {
    override fun systemRoot(): Preferences = FilePreferences(null, "")

    override fun userRoot(): Preferences = FilePreferences(null, "")

    init {
        privileged {
            System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory::class.qualifiedName!!)
        }
    }
}
