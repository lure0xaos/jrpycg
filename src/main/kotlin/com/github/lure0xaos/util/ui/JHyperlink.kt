package com.github.lure0xaos.util.ui

import java.awt.Color
import java.awt.Cursor
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import javax.swing.JLabel
import javax.swing.SwingConstants

class JHyperlink(text: String, location: String, tooltip: String = "") : JLabel(text, SwingConstants.CENTER) {
    private val normalColor = Color(0, 0, 178)
    private val hoverColor = Color(0, 0, 255)
    private val clickColor = Color(178, 0, 0)

    init {
        foreground = normalColor
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        toolTipText = tooltip
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                runCatching { Desktop.getDesktop().browse(URI(location)) }
            }

            override fun mousePressed(e: MouseEvent) {
                foreground = clickColor
            }

            override fun mouseReleased(e: MouseEvent) {
                foreground = hoverColor
            }

            override fun mouseEntered(e: MouseEvent) {
                foreground = hoverColor
            }

            override fun mouseExited(e: MouseEvent) {
                foreground = normalColor
            }
        })
    }
}
