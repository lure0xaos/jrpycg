package com.github.lure0xaos.jrpycg.ui.comp.builder

import com.github.lure0xaos.jrpycg.model.VarType
import com.github.lure0xaos.jrpycg.res.Res
import com.github.lure0xaos.jrpycg.res.icons.ResIcon
import com.github.lure0xaos.jrpycg.services.LocaleHolder
import com.github.lure0xaos.util.get
import com.github.lure0xaos.util.getResourceBundle
import com.github.lure0xaos.util.ui.JFormPanel
import com.github.lure0xaos.util.ui.swing
import java.awt.Component
import java.util.*
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JTextField
import javax.swing.plaf.basic.BasicComboBoxRenderer

class CheatVariablePanel(
    localeHolder: LocaleHolder,
    nameValue: String = "",
    labelValue: String = "",
    valueValue: String = "",
    typeValue: VarType = VarType.STR
) : JFormPanel() {
    private val resources: ResourceBundle = Res::class.getResourceBundle(CheatVariablePanel::class, localeHolder.locale)

    private val txtName = JTextField(nameValue)
    private val txtLabel = JTextField(labelValue)
    private val txtValue = JTextField(valueValue)
    private val cmbType: JComboBox<VarType> = JComboBox(VarType.entries.toTypedArray()).apply {
        renderer = object : BasicComboBoxRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component =
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                    .apply {
                        val varType = value as? VarType
                        text = (varType?.text ?: "")
                        icon = varType?.icon
                    }
        }
        selectedItem = typeValue
    }
    val nameValue: String
        get() = txtName.text
    val labelValue: String
        get() = txtLabel.text
    val valueValue: String
        get() = txtValue.text
    val fixedValue: Boolean
        get() = txtValue.text.isNotEmpty()
    val typeValue: VarType
        get() = cmbType.selectedItem as VarType

    init {
        addLabelControl(resources[LC_NAME], ResIcon.REQUIRED.icon, txtName, resources[LC_NAME_TOOLTIP])
        addLabelControl(resources[LC_LABEL], txtLabel, resources[LC_LABEL_TOOLTIP])
        addLabelControl(resources[LC_VALUE], txtValue, resources[LC_VALUE_TOOLTIP])
        addLabelControl(resources[LC_TYPE], ResIcon.REQUIRED.icon, cmbType.apply {
            swing {
                selectedIndex = 0
            }
        }, resources[LC_TYPE_TOOLTIP])
    }

    fun validateItem(): List<Pair<List<String>, JComponent>> = listOf<Pair<List<String>, JComponent>>() +
            (if (nameValue.isBlank()) listOf(listOf(resources[ERROR_NAME_BLANK]) to txtName) else listOf()) +
            (if (!nameValue.matches(Regex(REGEX_NAME))) listOf(listOf(resources[ERROR_NAME_INVALID]) to txtName) else listOf()) +
            (if (!labelValue.matches(Regex(REGEX_LABEL))) listOf(listOf(resources[ERROR_LABEL_INVALID]) to txtLabel) else listOf()) +
            (if (cmbType.selectedItem == null) listOf(listOf(resources[ERROR_TYPE_EMPTY]) to cmbType) else listOf())

    companion object {
        private const val LC_NAME = "name"
        private const val LC_NAME_TOOLTIP = "name.tooltip"
        private const val REGEX_NAME = "[._a-zA-Z0-9]+"
        private const val LC_LABEL = "label"
        private const val LC_LABEL_TOOLTIP = "label.tooltip"
        private const val REGEX_LABEL = "[.-a-zA-Z0-9 \\p{L}(),!?%+]+"
        private const val LC_VALUE = "value"
        private const val LC_VALUE_TOOLTIP = "value.tooltip"
        private const val LC_TYPE = "type"
        private const val LC_TYPE_TOOLTIP = "type.tooltip"

        private const val ERROR_NAME_BLANK = "validation.name.blank"
        private const val ERROR_NAME_INVALID = "validation.name.invalid"
        private const val ERROR_LABEL_INVALID = "validation.label.invalid"
        private const val ERROR_TYPE_EMPTY = "validation.type.empty"
    }
}
