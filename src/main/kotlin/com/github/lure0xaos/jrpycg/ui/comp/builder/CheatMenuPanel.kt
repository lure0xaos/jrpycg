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
        addLabelControl(resources[LC_NAME], ResIcon.REQUIRED.icon, txtName)
        addLabelControl(resources[LC_LABEL], txtLabel)
    }

    fun validateItem(): List<Pair<List<String>, JComponent>> {
        val errors: MutableList<Pair<List<String>, JComponent>> = mutableListOf()
        if (nameValue.isBlank()) errors += listOf(resources["validation.name.blank"]) to txtName
        return errors
    }

    companion object {
        private const val LC_NAME = "name"
        private const val LC_LABEL = "label"
    }
}
