package com.github.lure0xaos.util.ui.render

import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultTreeCellRenderer

class DecoratedTreeCellRenderer<E : Any>(
    private val decorator: JLabel.(
        tree: JTree, value: E, selected: Boolean, expanded: Boolean, leaf: Boolean, row:
        Int, hasFocus: Boolean
    ) -> Unit
) : DefaultTreeCellRenderer() {
    @Suppress("UNCHECKED_CAST")
    override fun getTreeCellRendererComponent(
        tree: JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ): Component =
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus).apply {
            if (value != null)
                decorator(tree, value as E, selected, expanded, leaf, row, hasFocus)
        }

}
