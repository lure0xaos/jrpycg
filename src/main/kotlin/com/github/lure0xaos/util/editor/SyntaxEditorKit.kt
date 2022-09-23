package com.github.lure0xaos.util.editor

import java.awt.Color
import javax.swing.text.StyledEditorKit
import javax.swing.text.ViewFactory

class SyntaxEditorKit(private val mimeType: String, patternColors: Map<Regex, Color>) : StyledEditorKit() {
    private val viewFactory: ViewFactory = SyntaxViewFactory(patternColors)

    override fun getContentType(): String =
        mimeType

    override fun getViewFactory(): ViewFactory =
        viewFactory
}
