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
import java.util.ResourceBundle
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
    private val cmbType: JComboBox<VarType> = JComboBox(VarType.values()).apply {
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
                        text = (if (index == -1) "" else (value as VarType).text)
                        icon = (if (index == -1) null else (value as VarType).icon)
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
        addLabelControl(resources[LC_NAME], ResIcon.REQUIRED.icon, txtName)
        addLabelControl(resources[LC_LABEL], txtLabel)
        addLabelControl(resources[LC_VALUE], txtValue)
        addLabelControl(resources[LC_TYPE], ResIcon.REQUIRED.icon, cmbType.apply {
            swing {
                selectedIndex = 0
            }
        })
    }

    fun validateItem(): List<Pair<List<String>, JComponent>> {
        val errors: MutableList<Pair<List<String>, JComponent>> = mutableListOf()
        if (nameValue.isBlank()) errors += listOf(resources["validation.name.blank"]) to txtName
        if (cmbType.selectedItem == null) errors += listOf(resources["validation.type.empty"]) to cmbType
        return errors
    }

    companion object {
        private const val LC_NAME = "name"
        private const val LC_LABEL = "label"
        private const val LC_VALUE = "value"
        private const val LC_TYPE = "type"
    }
}
