package com.github.lure0xaos.util.ui

import java.text.ParseException
import javax.swing.InputVerifier
import javax.swing.JComponent
import javax.swing.JFormattedTextField

class FormattedTextFieldVerifier : InputVerifier() {
    override fun verify(input: JComponent): Boolean {
        if (input is JFormattedTextField) {
            val formatter = input.formatter
            if (formatter != null) {
                val text = input.text
                return try {
                    formatter.stringToValue(text)
                    true
                } catch (pe: ParseException) {
                    false
                }
            }
        }
        return true
    }

    @Deprecated("Deprecated in Java", ReplaceWith("verify(input)"))
    override fun shouldYieldFocus(input: JComponent): Boolean {
        return verify(input)
    }

    override fun shouldYieldFocus(source: JComponent, target: JComponent?): Boolean {
        return verify(source) && verifyTarget(target)
    }
}
