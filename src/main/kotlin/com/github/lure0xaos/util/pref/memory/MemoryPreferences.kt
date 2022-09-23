package com.github.lure0xaos.util.pref.memory

import java.util.prefs.AbstractPreferences

class MemoryPreferences(parent: AbstractPreferences?, name: String) : AbstractPreferences(parent, name) {
    private val preferences: MutableMap<String, String> = mutableMapOf()
    private val children: MutableMap<String, MemoryPreferences> = mutableMapOf()

    override fun putSpi(key: String, value: String) {
        preferences[key] = value
    }

    override fun getSpi(key: String): String? = preferences[key]

    override fun removeSpi(key: String) {
        preferences.remove(key)
    }

    override fun removeNodeSpi() {
        parent()?.also { (it as? MemoryPreferences)?.children?.remove(name()) }
    }

    override fun keysSpi(): Array<String> = preferences.keys.toTypedArray()

    override fun childrenNamesSpi(): Array<String> = children.keys.toTypedArray()

    override fun childSpi(name: String): AbstractPreferences =
        if (children.containsKey(name)) children[name]!! else MemoryPreferences(this, name).also { children[name] = it }

    override fun syncSpi() {
    }

    override fun flushSpi() {
    }
}
