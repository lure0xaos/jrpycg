package com.github.lure0xaos.jrpycg.services

import com.github.lure0xaos.util.format
import com.github.lure0xaos.util.get
import com.github.lure0xaos.util.getResourceBundle
import com.github.lure0xaos.util.ui.urlEncode
import java.awt.Desktop
import java.net.URI
import java.util.ResourceBundle

class Email(localeHolder: LocaleHolder) {
    private val resources: ResourceBundle = Email::class.getResourceBundle(Email::class, localeHolder.locale)

    fun email(text: String = "", e: Throwable? = null) {
        runCatching {
            Desktop.getDesktop().mail(URI(buildString {
                append("mailto:")
                append(resources[LC_EMAIL])
                append("?subject=")
                append(resources[LC_SUBJECT].urlEncode())
                append("&body=")
                append(
                    resources.format(
                        LC_TEXT, mapOf(
                            PARAM_TEXT to text,
                            PARAM_ERROR to (e?.stackTraceToString() ?: "")
                        )
                    ).urlEncode()
                )
            }))
        }
    }

    companion object {
        private const val PARAM_TEXT: String = "text"
        private const val PARAM_ERROR: String = "error"
        private const val LC_SUBJECT: String = "subject"
        private const val LC_TEXT: String = "text"
        private const val LC_EMAIL: String = "email"

        fun install(localeHolder: LocaleHolder) {
            Thread.setDefaultUncaughtExceptionHandler { _, e ->
                Email(localeHolder).email("", e)
            }
        }
    }
}
