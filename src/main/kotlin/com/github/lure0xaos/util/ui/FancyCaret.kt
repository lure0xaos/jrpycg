package com.github.lure0xaos.util.ui

import java.awt.Graphics
import java.awt.Rectangle
import java.awt.geom.Rectangle2D
import javax.swing.text.BadLocationException
import javax.swing.text.DefaultCaret
import javax.swing.text.JTextComponent

internal class FancyCaret : DefaultCaret() {
    @Synchronized
    override fun damage(r: Rectangle?) {
        if (r == null) return
        x = r.x
        y = r.y
        height = r.height
        if (width <= 0) {
            width = component.width
        }
        repaint()
    }

    override fun paint(g: Graphics) {
        val comp: JTextComponent = component ?: return
        val dot: Int = dot
        val r: Rectangle2D?
        var dotChar: Char
        try {
            r = comp.modelToView2D(dot) ?: return
            dotChar = comp.getText(dot, 1)[0]
        } catch (e: BadLocationException) {
            return
        }
        if (x != r.x.toInt() || y != r.y.toInt()) {
            repaint()
            x = r.x.toInt()
            y = r.y.toInt()
            height = r.height.toInt()
        }
        g.color = comp.caretColor
        g.setXORMode(comp.background)
        if (dotChar == NL) {
            val diam = r.height
            paintCaret(g, r)
            width = (diam / 2 + 2).toInt()
            return
        }
        if (dotChar == TAB) {
            try {
                val nextR: Rectangle2D = comp.modelToView2D(dot + 1)
                if (r.y.toInt() == nextR.y.toInt() && r.x < nextR.x) {
                    width = nextR.x.toInt() - r.x.toInt()
                    paintCaret(g, r)
                    return
                } else {
                    dotChar = SPACE
                }
            } catch (e: BadLocationException) {
                dotChar = SPACE
            }
        }
        width = g.fontMetrics.charWidth(dotChar)
        paintCaret(g, r)
    }

    private fun paintCaret(g: Graphics, r: Rectangle2D) {
        if (isVisible) {
            damage(Rectangle(r.x.toInt(), r.y.toInt(), width, r.height.toInt()))
            g.fillRect(r.x.toInt(), r.y.toInt(), width, r.height.toInt())
        }
    }

    companion object {
        private const val NL = '\n'
        private const val SPACE = ' '
        private const val TAB = '\t'
    }
}
