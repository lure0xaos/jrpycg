package com.github.lure0xaos.util.ui

import java.awt.Font
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JLabel
import javax.swing.SwingConstants
import kotlin.math.min


class JSizingLabel(text: String, alignment: Int = SwingConstants.LEFT) : JLabel(text, alignment) {
    init {
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                fit(this@JSizingLabel)
            }
        })
    }

    private fun fit(label: JLabel) {
        swing {
            val componentWidth: Int = label.width
            val componentHeight: Int = label.height
            if (componentWidth != 0 && componentHeight != 0) {
                val labelFont: Font = label.font
                val labelText: String = label.text
                val stringWidth: Int = label.getFontMetrics(labelFont).stringWidth(labelText)
                val newFontSize: Float = labelFont.size2D * componentWidth / stringWidth
                val fontSizeToUse: Float = min(newFontSize, componentHeight * 0.75f)
                label.font = font.deriveFont(fontSizeToUse)
            }
        }
    }
}
