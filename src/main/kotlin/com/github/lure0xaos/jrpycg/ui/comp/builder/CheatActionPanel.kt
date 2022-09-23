package com.github.lure0xaos.jrpycg.ui.comp.builder

import com.github.lure0xaos.jrpycg.res.Res
import com.github.lure0xaos.jrpycg.res.icons.ResIcon
import com.github.lure0xaos.jrpycg.services.LocaleHolder
import com.github.lure0xaos.util.editor.createTextPane
import com.github.lure0xaos.util.get
import com.github.lure0xaos.util.getResourceBundle
import com.github.lure0xaos.util.ui.JFormPanel
import java.awt.Color
import java.awt.Font
import java.util.*
import javax.swing.JComponent
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.ScrollPaneConstants

class CheatActionPanel(
    localeHolder: LocaleHolder,
    nameValue: String = "",
    labelValue: String = "",
    valueValue: String = "",
) : JFormPanel() {
    private val resources: ResourceBundle = Res::class.getResourceBundle(CheatActionPanel::class, localeHolder.locale)

    private val txtName = JTextField(nameValue)
    private val txtLabel = JTextField(labelValue)
    private val txtValue = createTextPane(MIMETYPE, FONT, PATTERN_COLORS, toEditor(valueValue))
    private val scrollPane: JScrollPane = JScrollPane(
        txtValue,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
    )
    val nameValue: String
        get() = txtName.text
    val labelValue: String
        get() = txtLabel.text
    val valueValue: String
        get() = fromEditor(txtValue.text)

    init {
        addLabelControl(resources[LC_NAME], ResIcon.REQUIRED.icon, txtName, resources[LC_NAME_TOOLTIP])
        addLabelControl(resources[LC_LABEL], txtLabel, resources[LC_LABEL_TOOLTIP])
        addLabelControl(resources[LC_VALUE], scrollPane, resources[LC_VALUE_TOOLTIP])
    }

    fun validateItem(): List<Pair<List<String>, JComponent>> = listOf<Pair<List<String>, JComponent>>() +
            (if (nameValue.isBlank()) listOf(listOf(resources[ERROR_NAME_BLANK]) to txtName) else listOf()) +
            (if (!nameValue.matches(Regex(REGEX_NAME))) listOf(listOf(resources[ERROR_NAME_INVALID]) to txtName) else listOf()) +
            (if (!labelValue.matches(Regex(REGEX_LABEL))) listOf(listOf(resources[ERROR_LABEL_INVALID]) to txtLabel) else listOf())

    private fun toEditor(text: String): String = text.replace("\\n", System.lineSeparator())
    private fun fromEditor(text: String): String = text.replace(System.lineSeparator(), "\\n")

    companion object {
        val FONT = Font(Font.MONOSPACED, Font.PLAIN, 12)
        const val MIMETYPE = "text/python"

        val PATTERN_COLORS: Map<Regex, Color> = mapOf(
            Regex("(#.*$)") to Color(0x808080),
            Regex("(-\\?\\d\\+\\(\\.\\d\\+\\)\\?)") to Color(0x800080),
            Regex("(-\\?\\.\\d\\+)") to Color(0x800080),
            Regex("([.,0-9]+)") to Color(0x008000),
        )


        val PATTERN_COLORS0: Map<Regex, Color> = mapOf(
            Regex("([<>])") to Color(0x808000),
            Regex("<([^;]+)") to Color(0x008000),
            Regex(";(.*)") to Color(0x000080),
            Regex("(\\([a-zA-Z]+\\))") to Color(0x800080),
            Regex("=([^(\"]+)") to Color(0x800000)
        )

        private const val LC_NAME = "name"
        private const val LC_NAME_TOOLTIP = "name.tooltip"
        private const val REGEX_NAME = "[._a-zA-Z0-9]+"
        private const val LC_LABEL = "label"
        private const val LC_LABEL_TOOLTIP = "label.tooltip"
        private const val REGEX_LABEL = "[.-a-zA-Z0-9 \\p{L}(),!?%+]+"
        private const val LC_VALUE = "value"
        private const val LC_VALUE_TOOLTIP = "value.tooltip"

        private const val ERROR_NAME_BLANK = "validation.name.blank"
        private const val ERROR_NAME_INVALID = "validation.name.invalid"
        private const val ERROR_LABEL_INVALID = "validation.label.invalid"
        private const val ERROR_TYPE_EMPTY = "validation.type.empty"
    }
}
