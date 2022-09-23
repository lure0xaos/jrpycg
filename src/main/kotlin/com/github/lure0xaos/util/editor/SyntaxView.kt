package com.github.lure0xaos.util.editor

import java.awt.Color
import java.awt.Graphics2D
import java.util.TreeMap
import javax.swing.text.Element
import javax.swing.text.PlainDocument
import javax.swing.text.PlainView
import javax.swing.text.Utilities

class SyntaxView(element: Element, private val patternColors: Map<Regex, Color>) : PlainView(element) {
    init {
        document.putProperty(PlainDocument.tabSizeAttribute, 4)
    }

    override fun drawUnselectedText(graphics: Graphics2D, x: Float, y: Float, p0: Int, p1: Int): Float {
        val doc = document
        val text = doc.getText(p0, p1 - p0)
        val segment = lineBuffer
        val startMap = TreeMap<Int, Int>()
        val colorMap = TreeMap<Int, Color>()
        for ((key, value) in patternColors) {
            val findAll: Sequence<MatchResult> = key.findAll(text)
            for (matcher in findAll) {
                startMap[matcher.range.first] = matcher.range.last + 1
                colorMap[matcher.range.first] = value
            }
        }
        // TODO: check the map for overlapping parts
        var xx = x
        var i = 0
        for ((start, end) in startMap) {
            if (i < start) {
                graphics.color = Color.BLACK
                doc.getText(p0 + i, start - i, segment)
                xx = Utilities.drawTabbedText(segment, xx, y, graphics, this, i)
            }
            graphics.color = colorMap[start]
            i = end
            doc.getText(p0 + start, i - start, segment)
            xx = Utilities.drawTabbedText(segment, xx, y, graphics, this, start)
        }
        // Paint possible remaining text black
        if (i < text.length) {
            graphics.color = Color.BLACK
            doc.getText(p0 + i, text.length - i, segment)
            xx = Utilities.drawTabbedText(segment, xx, y, graphics, this, i)
        }
        return xx
    }
}
