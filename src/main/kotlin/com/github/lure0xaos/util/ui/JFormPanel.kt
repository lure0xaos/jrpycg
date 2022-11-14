package com.github.lure0xaos.util.ui

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

open class JFormPanel(
    private val constraints: GridBagConstraints.(JComponent) -> Unit = {},
    customizer: JFormPanel.() -> Unit = {}
) : JPanel(GridBagLayout()) {

    private var row: Int = 0

    private fun newLabelControl(label: String, icon: Icon?, tooltip: String?, control: JComponent) {
        add(JLabel(label, icon, SwingConstants.RIGHT).apply {
            if (tooltip != null) toolTipText = tooltip
        }, GridBagConstraints {
            gridx = 0
            gridy = row
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.LINE_END
            weightx = 0.5
            ipadx = 10
            ipady = 10
            insets = Insets(10, 10, 10, 10)
        })
        add(control, GridBagConstraints {
            gridx = 1
            gridy = row
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.LINE_START
            weightx = 1.0
            ipadx = 10
            ipady = 10
            insets = Insets(10, 10, 10, 10)
            constraints(control)
        })
        row++
    }

    fun addLabelControl(label: String, control: JComponent, tooltip: String): Unit =
        newLabelControl(label, null, tooltip, control)

    fun addLabelControl(label: String, icon: Icon, control: JComponent, tooltip: String): Unit =
        newLabelControl(label, icon, tooltip, control)

    fun addLabelControl(label: String, control: JComponent): Unit =
        newLabelControl(label, null, null, control)

    fun addLabelControl(label: String, icon: Icon, control: JComponent): Unit =
        newLabelControl(label, icon, null, control)

    init {
        customizer()
    }
}
