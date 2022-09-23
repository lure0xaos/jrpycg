package com.github.lure0xaos.util.ui.dc

import java.nio.file.Path
import java.util.Collections
import java.util.Enumeration
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

class DirectoryTreeNode(val directory: Path) : MutableTreeNode {
    constructor() : this(Path(""))

    private val parentNode: DirectoryTreeNode? = if (isRoot()) null else directory.parent?.let { DirectoryTreeNode(it) }
    private val childrenNodes: MutableList<DirectoryTreeNode> =
        if (isRoot()) directory.fileSystem.rootDirectories.map { DirectoryTreeNode(it) }.toMutableList()
        else mutableListOf()

    fun isRoot(): Boolean = directory.toString().isEmpty()

    override fun getChildAt(childIndex: Int): TreeNode = childrenNodes[childIndex]

    override fun getChildCount(): Int = childrenNodes.size

    override fun getParent(): TreeNode? = parentNode

    override fun getIndex(node: TreeNode?): Int = childrenNodes.indexOf(node)

    override fun getAllowsChildren(): Boolean = true

    override fun isLeaf(): Boolean = false

    override fun children(): Enumeration<out TreeNode> = Collections.enumeration(childrenNodes)

    override fun insert(child: MutableTreeNode?, index: Int) {
    }

    override fun remove(index: Int) {
    }

    override fun remove(node: MutableTreeNode?) {
    }

    override fun setUserObject(`object`: Any?) {
    }

    override fun removeFromParent() {
    }

    override fun setParent(newParent: MutableTreeNode?) {
    }

    fun update(): Boolean {
        val newPaths: List<Path> =
            runCatching { directory.listDirectoryEntries().filter { it.isDirectory() } }.getOrDefault(listOf())
        childrenNodes.filter { it.directory !in newPaths }.also { childrenNodes.removeAll(it) }
        childrenNodes +=
            newPaths.filter { path: Path -> path !in childrenNodes.map { it.directory } }.map { DirectoryTreeNode(it) }
        return true
    }

    override fun equals(other: Any?): Boolean =
        if (this === other) true else if (other !is DirectoryTreeNode) false else directory == other.directory

    override fun hashCode(): Int = directory.hashCode()

    override fun toString(): String =
        "DirectoryTreeNode(directory=$directory, parentNode=$parentNode, childrenNodes=$childrenNodes)"

}
