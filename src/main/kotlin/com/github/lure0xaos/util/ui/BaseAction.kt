package com.github.lure0xaos.util.ui

import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Icon

class BaseAction : AbstractAction {
    private val action: (ActionEvent) -> Unit

    constructor(name: String, action: (ActionEvent) -> Unit) : super(name) {
        this.action = action
    }

    constructor(name: String, icon: Icon, action: (ActionEvent) -> Unit) : super(name, icon) {
        this.action = action
    }

    override fun actionPerformed(e: ActionEvent): Unit = action(e)
}
