package com.github.lure0xaos.util.ui.key

import com.github.lure0xaos.util.get
import com.github.lure0xaos.util.getResourceBundle
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.Locale
import java.util.ResourceBundle
import javax.swing.JToggleButton

class JKeyButton(keyCombination: KeyCombination = KeyCombination(), locale: Locale = Locale.getDefault()) :
    JToggleButton(keyCombination.getString(), false), KeyListener, ActionListener, FocusListener {
    private val resources: ResourceBundle = JKeyButton::class.getResourceBundle(JKeyButton::class, locale)

    var keyCombination: KeyCombination = keyCombination
        set(value) {
            field = value
            deactivate()
        }

    init {
        margin = Insets(0, 0, 0, 0)
        addKeyListener(this)
        addActionListener(this)
        addFocusListener(this)
    }

    override fun keyTyped(e: KeyEvent) {
    }

    override fun keyPressed(e: KeyEvent) {
    }

    override fun keyReleased(e: KeyEvent) {
        onAssign(e)
    }

    override fun actionPerformed(e: ActionEvent) {
        if (isSelected) activate() else deactivate()
    }

    override fun focusGained(e: FocusEvent) {
    }

    override fun focusLost(e: FocusEvent) {
        deactivate()
    }

    private fun onAssign(e: KeyEvent) {
        if (isSelected) {
            keyCombination = KeyCombination(e)
            deactivate()
        }
    }

    private fun activate() {
        text = resources[LC_PRESS_KEY]
        isSelected = true
    }

    private fun deactivate() {
        text = keyCombination.getString()
        isSelected = false
    }

    companion object {
        private const val LC_PRESS_KEY = "press"
    }
}
