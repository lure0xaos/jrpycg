package com.github.lure0xaos.jrpycg.ui.comp.builder

import com.github.lure0xaos.jrpycg.res.Res
import com.github.lure0xaos.jrpycg.res.icons.ResIcon
import com.github.lure0xaos.jrpycg.services.LocaleHolder
import com.github.lure0xaos.util.get
import com.github.lure0xaos.util.getResourceBundle
import com.github.lure0xaos.util.ui.JFormPanel
import java.util.ResourceBundle
import javax.swing.JComponent
import javax.swing.JTextField


class CheatMenuPanel(localeHolder: LocaleHolder, nameValue: String = "", labelValue: String = "") : JFormPanel() {
    private val resources: ResourceBundle = Res::class.getResourceBundle(CheatMenuPanel::class, localeHolder.locale)

    private val txtName = JTextField(nameValue)
    private val txtLabel = JTextField(labelValue)
    val nameValue: String
        get() = txtName.text
    val labelValue: String
        get() = txtLabel.text

    init {
        addLabelControl(resources[LC_NAME], ResIcon.REQUIRED.icon, txtName, resources[LC_NAME_TOOLTIP])
        addLabelControl(resources[LC_LABEL], txtLabel, resources[LC_LABEL_TOOLTIP])
    }

    fun validateItem(): List<Pair<List<String>, JComponent>> = listOf<Pair<List<String>, JComponent>>() +
            (if (nameValue.isBlank()) listOf(listOf(resources[ERROR_NAME_BLANK]) to txtName) else listOf()) +
            (if (!nameValue.matches(Regex(REGEX_NAME))) listOf(listOf(resources[ERROR_NAME_INVALID]) to txtName) else listOf()) +
            (if (!labelValue.matches(Regex(REGEX_LABEL))) listOf(listOf(resources[ERROR_LABEL_INVALID]) to txtLabel) else listOf())

    companion object {
        private const val LC_NAME = "name"
        private const val LC_NAME_TOOLTIP = "name.tooltip"
        private const val REGEX_NAME = "[._a-zA-Z0-9]+"
        private const val LC_LABEL = "label"
        private const val LC_LABEL_TOOLTIP = "label.tooltip"
        private const val REGEX_LABEL = "[.-a-zA-Z0-9 \\p{L}(),!?%+]+"

        private const val ERROR_NAME_BLANK = "validation.name.blank"
        private const val ERROR_NAME_INVALID = "validation.name.invalid"
        private const val ERROR_LABEL_INVALID = "validation.label.invalid"
    }
}
