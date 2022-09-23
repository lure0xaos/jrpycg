package com.github.lure0xaos.jrpycg.ui.comp.system

import com.github.lure0xaos.jrpycg.res.Res
import com.github.lure0xaos.jrpycg.services.LocaleHolder
import com.github.lure0xaos.util.capitalizeWords
import com.github.lure0xaos.util.get
import com.github.lure0xaos.util.getResourceBundle
import com.github.lure0xaos.util.privileged
import com.github.lure0xaos.util.putClipboard
import com.github.lure0xaos.util.ui.RowTableModel
import java.awt.BorderLayout
import java.util.Properties
import java.util.ResourceBundle
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable

class SystemPanel(localeHolder: LocaleHolder) : JPanel(BorderLayout(10, 10)) {
    private val resources: ResourceBundle = Res::class.getResourceBundle(SystemPanel::class, localeHolder.locale)

    private val tableModel: RowTableModel<Pair<String, String>> = RowTableModel(
        listOf(
            resources[LC_HEADER_NAME] to Pair<String, String>::first,
            resources[LC_HEADER_VALUE] to Pair<String, String>::second,
        ),
        listOf(
            (resources[LC_ACTION_COPY] to null) to { _, _, item ->
                putClipboard(item.second)
            }
        ),
        privileged<Properties> { System.getProperties() }
            .map {
                it.key.toString().capitalizeWords(localeHolder.locale, ".") to
                        it.value.toString().replace(";", ";\n")
            }
            .sortedWith { a, b -> a.first.compareTo(b.first) },
        localeHolder.locale
    )

    init {
        add(JScrollPane(JTable(tableModel, tableModel)), BorderLayout.CENTER)
    }

    companion object {
        private const val LC_HEADER_NAME = "header.name"
        private const val LC_HEADER_VALUE = "header.value"
        private const val LC_ACTION_COPY = "action.copy"
    }
}
