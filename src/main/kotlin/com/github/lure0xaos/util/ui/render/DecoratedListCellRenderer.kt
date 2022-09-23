package com.github.lure0xaos.util.ui.render

import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer

class DecoratedListCellRenderer<E : Any>(
    private val decorator: JLabel.(tree: JList<E>, value: E, index: Int, selected: Boolean, hasFocus: Boolean) -> Unit
) : DefaultListCellRenderer(), ListCellRenderer<Any> {
    @Suppress("UNCHECKED_CAST")
    override fun getListCellRendererComponent(
        tree: JList<*>,
        value: Any?,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ): Component =
        super.getListCellRendererComponent(tree, value, index, selected, hasFocus).apply {
            if (value != null)
                decorator(tree as JList<E>, value as E, index, selected, hasFocus)
        }

}
