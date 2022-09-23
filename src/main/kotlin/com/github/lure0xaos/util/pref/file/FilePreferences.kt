package com.github.lure0xaos.util.pref.file

import com.github.lure0xaos.util.USER_HOME
import java.nio.file.Path
import java.util.prefs.AbstractPreferences
import java.util.prefs.BackingStoreException
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

internal class FilePreferences(parent: AbstractPreferences?, name: String) : AbstractPreferences(parent, name) {
    private val properties: MutableMap<String, String> = sortedMapOf()
    private val root: Path = USER_HOME
    private val path: Path = root.resolve("$fullName$EXT")

    init {
        syncSpi()
    }

    private val fullName: String
        get() = absolutePath().replace(SLASH, DOT)

    override fun putSpi(key: String, value: String) {
        require(EQ !in key) { "invalid key $key" }
        properties[key] = value
        flushSpi()
    }

    override fun getSpi(key: String): String? =
        syncSpi().let { properties[key] }

    override fun removeSpi(key: String) {
        properties.remove(key)
        flushSpi()
    }

    override fun removeNodeSpi() {
        path.deleteIfExists().also { properties.clear() }
    }

    override fun keysSpi(): Array<String> =
        syncSpi().let { properties.keys.toTypedArray() }

    override fun childrenNamesSpi(): Array<String> =
        root.listDirectoryEntries()
            .asSequence()
            .filter { it.isRegularFile() }
            .filter { it.name.endsWith(EXT) }
            .map { it.nameWithoutExtension }
            .filter { it.startsWith("$fullName$DOT") }
            .map { it.removePrefix("$fullName$DOT") }
            .toList()
            .toTypedArray()

    override fun childSpi(name: String): AbstractPreferences =
        FilePreferences(this, name)

    override fun syncSpi() {
        try {
            if (path.exists()) {
                properties.putAll(path.readLines().filter { !it.startsWith("#") }.associate {
                    it.substringBefore(EQ).trim(SPACE, TAB) to
                            it.substringAfter(EQ).trim(QUOTE, SPACE, TAB)
                })
            }
        } catch (e: Exception) {
            throw BackingStoreException(e)
        }
    }

    override fun flushSpi() {
        try {
            path.writeLines(properties.map { "${it.key}$EQ${it.value}" })
        } catch (e: Exception) {
            throw BackingStoreException(e)
        }
    }

    override fun equals(other: Any?): Boolean =
        if (this === other) true else if (other !is FilePreferences) false else absolutePath() == other.absolutePath()

    override fun hashCode(): Int = absolutePath().hashCode()

    companion object {
        private const val EXT: String = ".prefs"
        private const val DOT = '.'
        private const val EQ = '='
        private const val SLASH = '/'
        private const val SPACE = ' '
        private const val TAB = '\t'
        private const val QUOTE = '\"'
    }
}
