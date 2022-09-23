package com.github.lure0xaos.util.ui

import javax.swing.JTextPane
import javax.swing.SwingConstants
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument


class JWrappedLabel(text: String, align: Int = SwingConstants.CENTER) : JTextPane() {
    init {
        isEditable = false
        isOpaque = false
        setText(text)
        val styledDocument: StyledDocument = styledDocument
        val attributes = SimpleAttributeSet()
        StyleConstants.setAlignment(
            attributes, when (align) {
                SwingConstants.CENTER -> StyleConstants.ALIGN_CENTER
                SwingConstants.LEFT -> StyleConstants.ALIGN_LEFT
                SwingConstants.RIGHT -> StyleConstants.ALIGN_RIGHT
                else -> StyleConstants.ALIGN_JUSTIFIED
            }
        )
        styledDocument.setParagraphAttributes(0, styledDocument.length, attributes, false)
    }

}
