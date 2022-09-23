@file:Suppress("NOTHING_TO_INLINE", "unused")

package com.github.lure0xaos.util

import com.github.lure0xaos.util.log.Log
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.image.BufferedImage
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.nio.file.Path
import java.util.Locale
import java.util.ResourceBundle
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon
import kotlin.io.path.Path
import kotlin.math.abs
import kotlin.reflect.KClass

fun KClass<*>.resolveName(name: String): String =
    if (name.startsWith('/')) name
    else resolveName().substringBeforeLast('/') + '/' + name

fun KClass<*>.resolveName(): String = '/' + qualifiedName!!.replace('.', '/')

inline fun KClass<*>.findResource(name: String): URL? =
    java.getResource(resolveName(name).removePrefix(resolveName().substringBeforeLast('/') + '/')).also {
        if (it === null) {
            Log.warn { "$name (${resolveName(name)}) not found" }
        }
    }

inline fun KClass<*>.findResourceStream(name: String): InputStream? =
    java.getResourceAsStream(resolveName(name).removePrefix(resolveName().substringBeforeLast('/') + '/')).also {
        if (it === null) {
            Log.warn { "$name (${resolveName(name)}) not found" }
        }
    }

inline fun KClass<*>.findResourceBundle(name: String, locale: Locale = Locale.getDefault()): ResourceBundle? =
    resolveName(name).removePrefix("/").let {
        runCatching { ResourceBundle.getBundle(it, locale, java.classLoader) }
            .onFailure { Log.warn { "$name.properties not found" } }.getOrNull()
    }

inline fun KClass<*>.findResourceBundle(name: KClass<*>, locale: Locale = Locale.getDefault()): ResourceBundle? =
    findResourceBundle(name.resolveName(), locale)


inline fun KClass<*>.findLocalizedResource(name: String, locale: Locale = Locale.getDefault()): URL? =
    when {
        locale.language.isNotEmpty() && locale.country.isNotEmpty() -> arrayOf(
            "_${locale.language}_${locale.country}",
            "_${locale.language}", ""
        )

        locale.language.isNotEmpty() && locale.country.isEmpty() -> arrayOf("_${locale.language}", "")
        else -> arrayOf("")
    }
        .map { "${name.substringBeforeLast('.')}$it.${name.substringAfterLast('.')}" }
        .map { findResource(it) }
        .firstOrNull { it != null }

inline fun KClass<*>.getResource(name: String): URL =
    findResource(name) ?: error("$name (${resolveName(name)}) not found")


inline fun KClass<*>.getResourceStream(name: String): InputStream =
    findResourceStream(name) ?: error("$name (${resolveName(name)}) not found")

inline fun KClass<*>.getResourceBundle(name: String, locale: Locale = Locale.getDefault()): ResourceBundle =
    findResourceBundle(name, locale) ?: error("$name.properties not found")

inline fun KClass<*>.getResourceBundle(name: KClass<*>, locale: Locale = Locale.getDefault()): ResourceBundle =
    getResourceBundle(name.resolveName(), locale)

inline fun KClass<*>.getLocalizedResource(name: String, locale: Locale = Locale.getDefault()): URL =
    findLocalizedResource(name, locale) ?: error("$name (${resolveName(name)}) not found")

inline fun KClass<*>.findResourceLocales(name: String, defaultLocale: Locale = Locale.ROOT): Set<Locale> {
    val fullName = resolveName(name)
    return (if (findResource(fullName) === null) emptySet() else setOf(defaultLocale)) + Locale.getAvailableLocales()
        .filter { locale ->
            when {
                locale.language.isNotEmpty() && locale.country.isNotEmpty() -> arrayOf(
                    "_${locale.language}_${locale.country}"
                )

                locale.language.isNotEmpty() && locale.country.isEmpty() -> arrayOf("_${locale.language}")
                else -> emptyArray()
            }
                .map { "${fullName.substringBeforeLast('.')}$it.${fullName.substringAfterLast('.')}" }
                .firstNotNullOfOrNull { java.getResource(it) } != null
        }.toSet()
}

inline fun KClass<*>.findResourceBundleLocales(name: String, localeForRoot: Locale = Locale.ROOT): Set<Locale> =
    findResourceLocales("$name$EXT_PROPERTIES", localeForRoot)

fun String.toUrl(): URL? = runCatching { URI(this).toURL() }.getOrNull()

fun URL.toImageIcon(): ImageIcon = ImageIcon(this)

fun URL.toIcon(): Icon = ImageIcon(this)
fun URL.toImage(): Image = ImageIcon(this).image
fun URL.toBufferedImage(): BufferedImage = ImageIO.read(this)

val USER_HOME: Path = Path(System.getProperty("user.home"))
val USER_DIR: Path = Path(System.getProperty("user.dir"))
val USER_NAME: String = System.getProperty("user.name")
fun String.format(args: Map<String, String>): String =
    replace(SHORT_PLACEHOLDER) { result: MatchResult -> result.groupValues[1].let { args[it] ?: it } }

fun doubleApproximates(d1: Double, d2: Double, epsilon: Double = 0.000001): Boolean = abs(d1 - d2) < epsilon

fun Double.toRanged(min: Double, max: Double): Double =
    kotlin.math.min(max, kotlin.math.max(min, this))


fun ResourceBundle.format(key: String, args: Map<String, CharSequence>): String =
    get(key).replace(PLACEHOLDER) {
        args[it.groupValues[1]] ?: it.value.also {
            Log.warn { "$it not provided in $baseBundleName[$key]" }
        }
    }

operator fun ResourceBundle.get(key: String, def: String): String =
    if (containsKey(key)) getString(key) else def

operator fun ResourceBundle.get(key: String): String =
    get(key, key)

fun <E : Any> MutableList<E>.setAll(list: List<E>) {
    clear()
    this += list
}

fun putClipboard(data: String) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(data), null)
}

fun String.capitalizeWords(
    locale: Locale = Locale.getDefault(),
    vararg delimiters: String = arrayOf(" ", "\t")
): String =
    split(*delimiters).joinToString(" ") { s: String ->
        s.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

private val PLACEHOLDER: Regex = Regex("\\$\\{([^}]+)}")
private val SHORT_PLACEHOLDER: Regex = Regex("\\{([^}]+)}")
const val EXT_PROPERTIES: String = ".properties"
