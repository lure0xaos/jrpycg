package com.github.lure0xaos.jrpycg.model

import com.github.lure0xaos.util.get
import com.github.lure0xaos.util.getResourceBundle
import com.github.lure0xaos.util.ui.key.KeyCombination
import java.util.Locale
import java.util.ResourceBundle
import java.util.prefs.Preferences

class Settings(
    private val preferences: Preferences,
    enableCheat: Boolean, enableConsole: Boolean,
    enableDeveloper: Boolean, enableWrite: Boolean,
    enableRollback: Boolean, keyCheat: KeyCombination,
    keyConsole: KeyCombination, keyDeveloper: KeyCombination,
    keyWrite: KeyCombination, localeMenu: Locale
) {

    var enableCheat: Boolean
    var enableConsole: Boolean
    var enableDeveloper: Boolean
    var enableRollback: Boolean
    var enableWrite: Boolean
    var keyCheat: KeyCombination
    var keyConsole: KeyCombination
    var keyDeveloper: KeyCombination
    var keyWrite: KeyCombination
    var localeMenu: Locale


    init {
        this.localeMenu = preferences[PREF_LOCALE_MENU, localeMenu.language].let { Locale.forLanguageTag(it) }
        this.enableCheat = preferences.getBoolean(PREF_CHEAT, enableCheat)
        this.enableConsole = preferences.getBoolean(PREF_CONSOLE, enableConsole)
        this.enableDeveloper = preferences.getBoolean(PREF_DEVELOPER, enableDeveloper)
        this.enableRollback = preferences.getBoolean(PREF_ROLLBACK, enableRollback)
        this.enableWrite = preferences.getBoolean(PREF_WRITE, enableWrite)
        this.keyCheat = preferences[PREF_KEY_CHEAT, keyCheat.getString()]?.let { KeyCombination.parse(it) } ?: keyCheat
        this.keyConsole =
            preferences[PREF_KEY_CONSOLE, keyConsole.getString()]?.let { KeyCombination.parse(it) } ?: keyConsole
        this.keyDeveloper =
            preferences[PREF_KEY_DEVELOPER, keyDeveloper.getString()]?.let { KeyCombination.parse(it) } ?: keyDeveloper
        this.keyWrite = preferences[PREF_KEY_WRITE, keyWrite.getString()]?.let { KeyCombination.parse(it) } ?: keyWrite
    }

    fun setAll(
        enableCheat: Boolean, enableConsole: Boolean, enableDeveloper: Boolean,
        enableWrite: Boolean, enableRollback: Boolean,
        keyCheat: KeyCombination, keyConsole: KeyCombination,
        keyDeveloper: KeyCombination, keyWrite: KeyCombination,
        localeMenu: Locale
    ) {
        this.localeMenu = preferences[PREF_LOCALE_MENU, localeMenu.language].let { Locale.forLanguageTag(it) }
        this.enableCheat = preferences.getBoolean(PREF_CHEAT, enableCheat)
        this.enableConsole = preferences.getBoolean(PREF_CONSOLE, enableConsole)
        this.enableDeveloper = preferences.getBoolean(PREF_DEVELOPER, enableDeveloper)
        this.enableRollback = preferences.getBoolean(PREF_ROLLBACK, enableRollback)
        this.enableWrite = preferences.getBoolean(PREF_WRITE, enableWrite)
        this.keyCheat = preferences[PREF_KEY_CHEAT, keyCheat.getString()]?.let { KeyCombination.parse(it) } ?: keyCheat
        this.keyConsole =
            preferences[PREF_KEY_CONSOLE, keyConsole.getString()]?.let { KeyCombination.parse(it) } ?: keyConsole
        this.keyDeveloper =
            preferences[PREF_KEY_DEVELOPER, keyDeveloper.getString()]?.let { KeyCombination.parse(it) } ?: keyDeveloper
        this.keyWrite = preferences[PREF_KEY_WRITE, keyWrite.getString()]?.let { KeyCombination.parse(it) } ?: keyWrite
    }

    companion object {
        fun fromDefaults(preferences: Preferences): Settings =
            getDefaults(preferences).let {
                Settings(
                    preferences,
                    it.enableCheat,
                    it.enableConsole,
                    it.enableDeveloper,
                    it.enableWrite,
                    it.enableRollback,
                    it.keyCheat,
                    it.keyConsole,
                    it.keyDeveloper,
                    it.keyWrite,
                    it.localeMenu
                )
            }

        const val PREF_KEY_CHEAT: String = "keyCheat"
        const val PREF_KEY_CONSOLE: String = "keyConsole"
        const val PREF_KEY_DEVELOPER: String = "keyDeveloper"
        const val PREF_KEY_WRITE: String = "keyWrite"
        private const val PREF_CHEAT = "cheat"
        private const val PREF_CONSOLE = "console"
        private const val PREF_DEVELOPER = "developer"
        private const val PREF_LOCALE_MENU = "localeMenu"
        private const val PREF_ROLLBACK = "rollback"
        private const val PREF_WRITE = "write"

        private val bundle: ResourceBundle = Settings::class.getResourceBundle(Settings::class)
        private fun getDefaults(preferences: Preferences): Settings = Settings(
            preferences,
            bundle[PREF_CHEAT].toBooleanStrict(),
            bundle[PREF_CONSOLE].toBooleanStrict(),
            bundle[PREF_DEVELOPER].toBooleanStrict(),
            bundle[PREF_WRITE].toBooleanStrict(),
            bundle[PREF_ROLLBACK].toBooleanStrict(),
            bundle[PREF_KEY_CHEAT].let { KeyCombination.parse(it) },
            bundle[PREF_KEY_CONSOLE].let { KeyCombination.parse(it) },
            bundle[PREF_KEY_DEVELOPER].let { KeyCombination.parse(it) },
            bundle[PREF_KEY_WRITE].let { KeyCombination.parse(it) },
            bundle[PREF_LOCALE_MENU].let { Locale.forLanguageTag(it) }
        )
    }
}
