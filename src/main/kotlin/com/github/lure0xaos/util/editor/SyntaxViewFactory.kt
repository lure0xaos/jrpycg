package com.github.lure0xaos.util.editor

import java.awt.Color
import javax.swing.text.Element
import javax.swing.text.View
import javax.swing.text.ViewFactory

class SyntaxViewFactory(private val patternColors: Map<Regex, Color>) : ViewFactory {
    override fun create(element: Element): View = SyntaxView(element, patternColors)
}
