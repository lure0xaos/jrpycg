package com.github.lure0xaos.jrpycg.ui.comp.builder

import com.github.lure0xaos.jrpycg.ui.model.UiModelItem
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.TransferHandler
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

internal class TreeTransferHandler : TransferHandler() {
    private val nodesFlavor: DataFlavor
    private val flavors: Array<DataFlavor>
    private lateinit var nodesToRemove: Array<DefaultMutableTreeNode>

    init {
        val mimeType =
            "${DataFlavor.javaJVMLocalObjectMimeType};class=\"${Array<DefaultMutableTreeNode>::class.java.name}\""
        nodesFlavor = DataFlavor(mimeType)
        flavors = arrayOf(nodesFlavor)
    }

    override fun canImport(support: TransferSupport): Boolean {
        if (!support.isDrop) return false
        support.setShowDropLocation(true)
        if (!support.isDataFlavorSupported(nodesFlavor)) return false
        val tree = support.component as JTree
        val path = (support.dropLocation as JTree.DropLocation).path
        val dropRow = tree.getRowForPath(path)
        if (UiModelItem.fromTreeNode(path.lastPathComponent).isVariable) return false
        for (selRow in tree.selectionRows!!) {
            if (selRow == dropRow) return false
            for (offspring in
            ((tree.getPathForRow(selRow).lastPathComponent as DefaultMutableTreeNode).depthFirstEnumeration())) {
                if (tree.getRowForPath(TreePath((offspring as DefaultMutableTreeNode).path)) == dropRow) return false
            }
        }
        return true
    }

    override fun createTransferable(c: JComponent): Transferable? {
        val tree = c as JTree
        val paths = tree.selectionPaths ?: return null
        val copies: MutableList<DefaultMutableTreeNode> = mutableListOf()
        val toRemove: MutableList<DefaultMutableTreeNode> = mutableListOf()
        val firstNode = paths[0].lastPathComponent as DefaultMutableTreeNode
        val doneItems: HashSet<TreeNode> = LinkedHashSet(paths.size)
        val copy = copy(firstNode, doneItems, tree)
        copies.add(copy)
        toRemove.add(firstNode)
        for (i in 1 until paths.size) {
            val next = paths[i].lastPathComponent as DefaultMutableTreeNode
            if (doneItems.contains(next)) {
                continue
            }
            if (next.level < firstNode.level) {
                break
            } else if (next.level > firstNode.level) {
                copy.add(copy(next, doneItems, tree))
            } else {
                copies.add(copy(next, doneItems, tree))
                toRemove.add(next)
            }
            doneItems.add(next)
        }
        nodesToRemove = toRemove.toTypedArray()
        return NodesTransferable(copies.toTypedArray(), flavors, nodesFlavor)
    }

    private fun copy(node: DefaultMutableTreeNode, doneItems: HashSet<TreeNode>, tree: JTree): DefaultMutableTreeNode {
        val copy = DefaultMutableTreeNode(node.userObject)
        doneItems.add(node)
        for (i in 0 until node.childCount) {
            copy.add(copy((node as TreeNode).getChildAt(i) as DefaultMutableTreeNode, doneItems, tree))
        }
        tree.expandRow(tree.getRowForPath(TreePath(copy.path)))
        return copy
    }

    override fun exportDone(source: JComponent, data: Transferable, action: Int) {
        if (action and MOVE == MOVE) {
            val model = (source as JTree).model as DefaultTreeModel
            for (defaultMutableTreeNode in nodesToRemove) {
                model.removeNodeFromParent(defaultMutableTreeNode)
            }
        }
    }

    override fun getSourceActions(c: JComponent): Int =
        COPY_OR_MOVE

    override fun importData(support: TransferSupport): Boolean {
        if (!canImport(support)) {
            return false
        }
        @Suppress("UNCHECKED_CAST")
        val nodes: Array<DefaultMutableTreeNode> =
            support.transferable.getTransferData(nodesFlavor) as Array<DefaultMutableTreeNode>
        val dl = support.dropLocation as JTree.DropLocation
        val childIndex = dl.childIndex
        val parent = dl.path.lastPathComponent as DefaultMutableTreeNode
        val jTree = support.component as JTree
        val model = jTree.model as DefaultTreeModel
        var index: Int = if (childIndex == -1) parent.childCount else childIndex
        for (node in nodes) {
            model.insertNodeInto(node, parent, index++)
        }
        jTree.expandPath(TreePath(model.getPathToRoot(parent)))
        return true
    }

    override fun toString(): String = javaClass.name

}
