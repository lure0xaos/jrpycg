package com.github.lure0xaos.util.ui.dc

import java.nio.file.Path
import java.util.Collections
import java.util.Enumeration
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

class DirectoryTreeNode private constructor(val directory: Path, private val parentNode: DirectoryTreeNode?) :
    MutableTreeNode, Comparable<DirectoryTreeNode> {

    constructor() : this(Path(""), null)

    private val childrenNodes: MutableList<DirectoryTreeNode>

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

    @Synchronized
    fun update(): Boolean {
        val newPaths: List<Path> =
            runCatching { directory.listDirectoryEntries().filter { it.isDirectory() } }.getOrDefault(listOf())
        val toRemove = childrenNodes.filter { it.directory !in newPaths }
        val toRemoveSize = toRemove.size
        if (toRemove.isNotEmpty()) childrenNodes.removeAll(toRemove)
        val toAdd = newPaths.filter { path: Path -> path !in childrenNodes.map { it.directory } }.map {
            DirectoryTreeNode(it, this)
        }
        val toAddSize = toAdd.size
        if (toAdd.isNotEmpty()) childrenNodes += toAdd
        if (toAddSize != 0)
            childrenNodes.sort()
        return toRemoveSize != 0 || toAddSize != 0
    }

    override fun compareTo(other: DirectoryTreeNode): Int =
        directory.compareTo(other.directory)

    override fun equals(other: Any?): Boolean =
        if (this === other) true else if (other !is DirectoryTreeNode) false else directory == other.directory

    override fun hashCode(): Int =
        directory.hashCode()

    override fun toString(): String =
        "DirectoryTreeNode(directory=$directory, hasChildren=${childrenNodes.isNotEmpty()})"

    init {
        this.childrenNodes =
            if (isRoot()) directory.fileSystem.rootDirectories.map { DirectoryTreeNode(it, this) }
                .sorted().toMutableList()
            else mutableListOf()
    }

}
