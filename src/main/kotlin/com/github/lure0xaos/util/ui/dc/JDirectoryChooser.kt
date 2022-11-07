package com.github.lure0xaos.util.ui.dc

import com.github.lure0xaos.util.ui.dialog.ButtonType
import com.github.lure0xaos.util.ui.dialog.Closing
import com.github.lure0xaos.util.ui.dialog.DialogType
import com.github.lure0xaos.util.ui.dialog.UIDialog
import com.github.lure0xaos.util.ui.render.DecoratedTreeCellRenderer
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dialog
import java.nio.file.Path
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTree
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeWillExpandListener
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel
import kotlin.io.path.exists

class JDirectoryChooser(
    private val customizer: UIDialog<JDirectoryChooser, Path>.() -> Unit = {},
    private val decorator: JLabel.(
        tree: JTree, value: DirectoryTreeNode, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean
    ) -> Unit = { _, _, _, _, _, _, _ -> }

) : JPanel(BorderLayout()) {
    private val treeModel: DefaultTreeModel = DefaultTreeModel(DirectoryTreeNode())
    private val tree: JTree = JTree(treeModel).apply {
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        isRootVisible = false
        cellRenderer =
            DecoratedTreeCellRenderer<DirectoryTreeNode> { jTree, value, sel, expanded, leaf, row, hasFocus ->
                text = pathFromNode(value)?.let {
                    if (it.nameCount == 0) it.toString() else it.getName(it.nameCount - 1).toString()
                }
                decorator(jTree, value, sel, expanded, leaf, row, hasFocus)
            }
        addTreeWillExpandListener(object : TreeWillExpandListener {
            override fun treeWillExpand(event: TreeExpansionEvent) {
                if (isCollapsed(event.path)) updateTree(event.path)
            }

            override fun treeWillCollapse(event: TreeExpansionEvent) {
            }
        })
    }

    var selectedDirectory: Path?
        get() = tree.selectionPath?.let { nodeFromTreePath(it).directory }
        set(value) {
            tree.selectionPath = value?.let { loadDirectory(it) }
                ?.let { findNode(treeModel, value) }?.let { TreePath(treeModel.getPathToRoot(it)) }
        }

    private fun updateTree(path: TreePath) {
        nodeFromTreePath(path).also { node -> if (!node.isRoot()) updateReload(node) }.also { node: DirectoryTreeNode ->
            if (node.isRoot()) {
                for (child in node.children()) {
                    expandPath((child as DirectoryTreeNode).directory)
                }
            }
        }
    }

    private fun nodeFromTreePath(path: TreePath): DirectoryTreeNode {
        return path.lastPathComponent as DirectoryTreeNode
    }

    private fun pathFromNode(value: Any): Path? = (value as? DirectoryTreeNode)?.directory

    init {
        add(JScrollPane(tree), BorderLayout.CENTER)
    }

    fun showDialog(owner: Component, title: String, validator: (Path) -> Boolean = { true }): Path? =
        UIDialog(
            owner,
            title,
            Dialog.ModalityType.APPLICATION_MODAL,
            this,
            DialogType.OK_CANCEL,
            { buttonType: ButtonType ->
                if (buttonType.isCancel) null else
                    selectedDirectory
            },
            {
                if (!tree.selectionModel.isSelectionEmpty && selectedDirectory?.let { validator(it) } == true)
                    emptyList()
                else
                    listOf<Pair<List<String>, JComponent>>(
                        listOf(/*resources[""]*/"") to this
                    )
            },
            Closing.DISPOSE
        ).apply(customizer).showAndWait()

    private fun findNode(model: DefaultTreeModel, node: Path): DirectoryTreeNode? {
        return findNode(model, node, model.root as DirectoryTreeNode)
    }

    private fun findNode(model: DefaultTreeModel, node: Path, cur: DirectoryTreeNode): DirectoryTreeNode? {
        if (cur.directory == node) return cur
        for (child in cur.children()) {
            val findNode = findNode(model, node, child as DirectoryTreeNode)
            if (findNode != null)
                return findNode
        }
        return null
    }

    private fun loadDirectory(directory: Path): Boolean {
        if (directory.exists()) {
            val paths: MutableList<Path> = mutableListOf()
            run {
                var path: Path? = directory
                while (path != null) {
                    paths.add(0, path)
                    path = path.parent
                }
            }
            paths.forEach { path ->
                findNode(treeModel, path)?.also { node -> updateReload(node) }
            }
            return true
        }
        return false
    }

    private fun updateReload(node: DirectoryTreeNode) {
        val selectionPath = tree.selectionPath
        val path = selectionPath?.let { nodeFromTreePath(it).directory }
        if (node.update()) treeModel.reload(node)
        if (selectionPath != tree.selectionPath) {
            tree.selectionPath = path?.let { findNode(treeModel, path) }?.let { TreePath(treeModel.getPathToRoot(it)) }
        }
    }

    private fun expandPath(path: Path) {
        findNode(treeModel, path)?.let { TreePath(treeModel.getPathToRoot(it)) }?.also {
            if (tree.isCollapsed(it)) tree.expandPath(it)
        }
    }

}
