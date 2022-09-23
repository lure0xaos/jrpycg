package com.github.lure0xaos.jrpycg.services

import com.github.lure0xaos.util.format
import com.github.lure0xaos.util.get
import com.github.lure0xaos.util.getResourceBundle
import com.github.lure0xaos.util.log.Log
import com.github.lure0xaos.util.ui.dialog.ButtonFactory
import com.github.lure0xaos.util.ui.dialog.ButtonType
import com.github.lure0xaos.util.ui.dialog.DialogType
import com.github.lure0xaos.util.ui.dialog.UIDialog
import com.github.lure0xaos.util.ui.dialog.alertError
import com.github.lure0xaos.util.ui.urlEncode
import java.awt.Component
import java.awt.Desktop
import java.net.URI
import java.util.ResourceBundle
import javax.swing.JPanel

class Email(localeHolder: LocaleHolder) {
    private val resources: ResourceBundle = Email::class.getResourceBundle(Email::class, localeHolder.locale)

    fun email(text: String = "", error: Throwable? = null) {
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
                            PARAM_ERROR to (error?.stackTraceToString() ?: "")
                        )
                    ).urlEncode()
                )
            }))
        }
    }

    fun ask(
        parent: Component,
        text: String = resources[LC_SUBJECT],
        error: Throwable,
        panelCustomizer: JPanel.() -> Unit = {},
        dialogCustomizer: UIDialog<JPanel, ButtonType>.() -> Unit = {}
    ): Boolean {
        return ButtonType.NO == parent.alertError(
            error = error,
            message = text,
            dialogType = DialogType.YES_NO,
            factory = {
                ButtonFactory.createButton(it).apply {
                    this.text = when (it) {
                        ButtonType.YES -> resources[LC_BUTTON_YES]
                        ButtonType.NO -> resources[LC_BUTTON_NO]
                        else -> ""
                    }
                }
            },
            panelCustomizer = panelCustomizer,
            dialogCustomizer = dialogCustomizer
        )
    }

    fun askAndEmail(
        parent: Component,
        text: String = resources[LC_SUBJECT],
        error: Throwable,
        panelCustomizer: JPanel.() -> Unit = {},
        dialogCustomizer: UIDialog<JPanel, ButtonType>.() -> Unit = {}
    ) {
        if (ask(parent, text, error, panelCustomizer, dialogCustomizer)) email(text, error)
    }

    companion object {
        private const val PARAM_TEXT: String = "text"
        private const val PARAM_ERROR: String = "error"
        private const val LC_SUBJECT: String = "subject"
        private const val LC_TEXT: String = "text"
        private const val LC_EMAIL: String = "email"
        private const val LC_BUTTON_YES: String = "button.yes"
        private const val LC_BUTTON_NO: String = "button.no"

        fun install(parent: Component, localeHolder: LocaleHolder, panelCustomizer: JPanel.() -> Unit = {}) {
            Thread.setDefaultUncaughtExceptionHandler { _, error: Throwable ->
                Log.error(error) { error.localizedMessage ?: "" }
                Email(localeHolder).askAndEmail(parent = parent, error = error, panelCustomizer = panelCustomizer) {}
            }
        }

    }
}
