package com.github.lure0xaos.jrpycg.ui.comp.creator

import com.github.lure0xaos.jrpycg.model.ModelItem
import com.github.lure0xaos.jrpycg.model.VarType
import com.github.lure0xaos.jrpycg.res.Res
import com.github.lure0xaos.jrpycg.services.LocaleHolder
import com.github.lure0xaos.jrpycg.services.ScriptConverter
import com.github.lure0xaos.util.editor.createTextPane
import com.github.lure0xaos.util.getResourceBundle
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.util.*
import javax.swing.*

class CreatorPanel(localeHolder: LocaleHolder) : JPanel(BorderLayout()) {
    private val resources: ResourceBundle = Res::class.getResourceBundle(CreatorPanel::class, localeHolder.locale)

    var isChanged: Boolean
        get() = prevText != text
        set(value) {
            if (!value) prevText = text
        }
    private var prevText: String = ""

    private val content: JTextPane = createTextPane(MIMETYPE, FONT, PATTERN_COLORS, "") {
        addFocusListener(object : FocusListener {
            override fun focusGained(e: FocusEvent) {
                content.repaint()
                scrollPane.repaint()
            }

            override fun focusLost(e: FocusEvent) {
                if (text != prevText) {
                    prevText = text
                    isChanged = true
                    checkErrors()
                }
            }
        })
    }

    private val scrollPane: JScrollPane = JScrollPane(
        content,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
    ).also {
        add(it, BorderLayout.CENTER)
    }

    fun onShow(): Unit =
        this.requestFocus()

    fun setScriptUnforced(script: List<String>) {
        if (!this.isFocusOwner) this.script = script
    }

    private var text: String
        get() = content.text
        set(text) {
            content.text = text
        }

    private fun decorateError(errors: Collection<String?>) {
        if (errors.isEmpty()) {
            classRemove(this)
            content.toolTipText = null
        } else {
            classAdd(this)
            content.toolTipText = errors.joinToString("\n")
        }
    }

    private fun classAdd(source: JComponent) {
        source.border = BorderFactory.createLineBorder(Color.RED)
    }

    private fun classRemove(source: JComponent) {
        source.border = BorderFactory.createEmptyBorder()
    }

    var script: List<String>
        get() = text.split("\n").map { it.trim() }.toList()
        set(value) {
            val script = value.joinToString("\n") { it.trim() }
            if (text != script) {
                text = script
                scrollDown()
            }
        }

    val callbackNewRootMenu: (String, String) -> Unit =
        { nameValue: String, labelValue: String ->
            text += '\n' + ScriptConverter.toScript(ModelItem.createRoot().createMenu(nameValue, labelValue)).first()
            scrollDown()
        }
    val callbackNewRootItem: (String, String, String, VarType) -> Unit =
        { nameValue: String, labelValue: String, valueValue: String, typeValue: VarType ->
            text += '\n' + ScriptConverter.toScript(
                ModelItem.createRoot().createVariable(nameValue, labelValue, valueValue, typeValue)
            ).first()
            scrollDown()
        }
    val callbackNewRootAction: (String, String, String) -> Unit =
        { nameValue: String, labelValue: String, valueValue: String ->
            text += '\n' + ScriptConverter.toScript(
                ModelItem.createRoot().createAction(nameValue, labelValue, valueValue)
            ).first()
            scrollDown()
        }

    fun checkErrors(): Result<ModelItem> =
        runCatching { (ScriptConverter.fromScript(script)) }
            .onFailure { decorateError(listOf(it.localizedMessage)) }
            .onSuccess { decorateError(listOf()) }

    private fun scrollDown() {
        content.caretPosition = content.document.length
        scrollPane.verticalScrollBar.apply {
            value = maximum
        }
    }

    companion object {
        val PATTERN_COLORS: Map<Regex, Color> = mapOf(
            Regex("([<>])") to Color(0x808000),
            Regex("<([^;]+)") to Color(0x008000),
            Regex(";(.*)") to Color(0x000080),
            Regex("(\\([a-zA-Z]+\\))") to Color(0x800080),
            Regex("=([^(\"]+)") to Color(0x800000)
        )
        val FONT = Font(Font.MONOSPACED, Font.PLAIN, 12)
        const val MIMETYPE = "text/rpycg"
    }
}
