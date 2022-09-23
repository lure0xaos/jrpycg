package com.github.lure0xaos.util.editor

import java.awt.Color
import java.awt.Font
import javax.swing.JTextPane

fun createTextPane(
    mimeType: String, font: Font, colors: Map<Regex, Color>, defaultValue: String = "", block: JTextPane.() -> Unit = {}
): JTextPane =
    JTextPane().apply {
        this.font = font
        setEditorKitForContentType(mimeType, SyntaxEditorKit(mimeType, colors))
        contentType = mimeType
        text = defaultValue
        block()
    }
