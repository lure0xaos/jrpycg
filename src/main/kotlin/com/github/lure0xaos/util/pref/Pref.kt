package com.github.lure0xaos.util.pref

import java.util.prefs.Preferences

operator fun Preferences.set(key: String, value: String): Unit =
    put(key, value)

operator fun Preferences.get(key: String): String? =
    get(key, null)


operator fun Preferences.set(key: String, value: Int): Unit =
    putInt(key, value)

operator fun Preferences.get(key: String, default: Int): Int =
    getInt(key, default)


operator fun Preferences.set(key: String, value: Long): Unit =
    putLong(key, value)

operator fun Preferences.get(key: String, default: Long): Long =
    getLong(key, default)


operator fun Preferences.set(key: String, value: Double): Unit =
    putDouble(key, value)

operator fun Preferences.get(key: String, default: Double): Double =
    getDouble(key, default)
