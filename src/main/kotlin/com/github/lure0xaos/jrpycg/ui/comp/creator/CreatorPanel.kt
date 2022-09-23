package com.github.lure0xaos.jrpycg.ui.comp.creator

import com.github.lure0xaos.jrpycg.model.ModelItem
import com.github.lure0xaos.jrpycg.model.VarType
import com.github.lure0xaos.jrpycg.res.Res
import com.github.lure0xaos.jrpycg.services.LocaleHolder
import com.github.lure0xaos.jrpycg.services.ScriptConverter
import com.github.lure0xaos.util.getResourceBundle
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.util.ResourceBundle
import java.util.TreeMap
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.ScrollPaneConstants
import javax.swing.text.Element
import javax.swing.text.PlainDocument
import javax.swing.text.PlainView
import javax.swing.text.StyledEditorKit
import javax.swing.text.Utilities
import javax.swing.text.View
import javax.swing.text.ViewFactory

class CreatorPanel(localeHolder: LocaleHolder) : JPanel(BorderLayout()) {
    private val resources: ResourceBundle = Res::class.getResourceBundle(CreatorPanel::class, localeHolder.locale)

    var isChanged: Boolean
        get() = prevText != text
        set(value) {
            if (!value) prevText = text
        }
    private var prevText: String = ""

    private val content: JTextPane = JTextPane()
    private val scrollPane: JScrollPane

    fun onShow(): Unit =
        this.requestFocus()

    fun setScriptUnforced(script: List<String>) {
        if (!this.isFocusOwner) this.script = script
    }

    private var text: String
        get() = content.text
        private set(text) {
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

    init {
        content.font = FONT
        content.setEditorKitForContentType(MIMETYPE, RPyCGEditorKit(PATTERN_COLORS))
        content.contentType = MIMETYPE
        scrollPane = JScrollPane(
            content,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
        add(scrollPane, BorderLayout.CENTER)
        this.content.addFocusListener(object : FocusListener {
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

    fun checkErrors(): Result<ModelItem> {
        return runCatching { (ScriptConverter.fromScript(script)) }
            .onFailure { decorateError(listOf(it.localizedMessage)) }
            .onSuccess { decorateError(listOf()) }
    }

    private fun scrollDown() {
        content.caretPosition = content.document.length
        val scrollBar = scrollPane.verticalScrollBar
        scrollBar.value = scrollBar.maximum
    }

    private class RPyCGEditorKit(patternColors: Map<Regex, Color>) : StyledEditorKit() {
        private val viewFactory: ViewFactory = RPyCGViewFactory(patternColors)

        override fun getContentType(): String =
            MIMETYPE

        override fun getViewFactory(): ViewFactory =
            viewFactory
    }

    private class RPyCGView(element: Element, private val patternColors: Map<Regex, Color>) : PlainView(element) {
        init {
            document.putProperty(PlainDocument.tabSizeAttribute, 4)
        }

        override fun drawUnselectedText(graphics: Graphics2D, x: Float, y: Float, p0: Int, p1: Int): Float {
            var xx = x
            val doc = document
            val text = doc.getText(p0, p1 - p0)
            val segment = lineBuffer
            val startMap = TreeMap<Int, Int>()
            val colorMap = TreeMap<Int, Color>()
            for ((key, value) in patternColors) {
                val findAll: Sequence<MatchResult> = key.findAll(text)
                for (matcher in findAll) {
                    startMap[matcher.range.first] = matcher.range.last + 1
                    colorMap[matcher.range.first] = value
                }
            }
            // TODO: check the map for overlapping parts
            var i = 0
            for ((start, end) in startMap) {
                if (i < start) {
                    graphics.color = Color.BLACK
                    doc.getText(p0 + i, start - i, segment)
                    xx = Utilities.drawTabbedText(segment, xx, y, graphics, this, i)
                }
                graphics.color = colorMap[start]
                i = end
                doc.getText(p0 + start, i - start, segment)
                xx = Utilities.drawTabbedText(segment, xx, y, graphics, this, start)
            }
            // Paint possible remaining text black
            if (i < text.length) {
                graphics.color = Color.BLACK
                doc.getText(p0 + i, text.length - i, segment)
                xx = Utilities.drawTabbedText(segment, xx, y, graphics, this, i)
            }
            return xx
        }
    }

    private class RPyCGViewFactory(private val patternColors: Map<Regex, Color>) : ViewFactory {
        override fun create(element: Element): View = RPyCGView(element, patternColors)
    }

    companion object {
        val PATTERN_COLORS: Map<Regex, Color> = mapOf(
            Regex("([<>])") to Color(0x808000),
            Regex("<([^;]+)") to Color(0x008000),
            Regex(";(.*)") to Color(0x000080),
            Regex("(\\([a-zA-Z]+\\))") to Color(0x800080),
            Regex("=([^(\"]+)") to Color(0x800000)
        )
        private val FONT = Font(Font.MONOSPACED, Font.PLAIN, 12)
        private const val MIMETYPE = "text/rpycg"
    }
}
