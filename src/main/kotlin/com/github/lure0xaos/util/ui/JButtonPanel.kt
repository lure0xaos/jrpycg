package com.github.lure0xaos.util.ui

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JButton
import javax.swing.JPanel

open class JButtonPanel(
    buttons: Map<JButton, GridBagConstraints.() -> Unit>,
    customizer: JButtonPanel.() -> Unit = {}
) : JPanel(GridBagLayout()) {

    constructor(buttons: Collection<JButton>, customizer: JButtonPanel.() -> Unit = {}) :
            this(buttons.associateWith { {} }, customizer)

    constructor(vararg buttons: JButton, customizer: JButtonPanel.() -> Unit = {}) : this(buttons.toList(), customizer)

    private var col: Int = 0

    init {
        buttons.forEach {
            add(it.key, GridBagConstraints {
                gridx = col
                gridy = 0
                fill = GridBagConstraints.HORIZONTAL
                anchor = GridBagConstraints.LINE_START
                weightx = 1.0
                insets = Insets(10, 10, 10, 10)
                (it.value)()
            })
            col++
        }
        customizer()
    }

}
