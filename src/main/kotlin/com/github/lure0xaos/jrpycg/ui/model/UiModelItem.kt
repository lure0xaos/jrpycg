package com.github.lure0xaos.jrpycg.ui.model

import com.github.lure0xaos.jrpycg.model.ModelType
import com.github.lure0xaos.jrpycg.model.VarType
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode

class UiModelItem private constructor(
    private val menuType: ModelType,
    var name: String,
    var label: String,
    var value: String,
    var type: VarType
) {
    val isMenu: Boolean = menuType == ModelType.MENU
    val isVariable: Boolean = menuType == ModelType.VAR
    val isAction: Boolean = menuType == ModelType.ACTION
    val isRoot: Boolean = menuType == ModelType.ROOT

    fun createMenu(
        model: DefaultTreeModel,
        name: String,
        label: String,
        initializer: UiModelItem.() -> Unit = { }
    ): UiModelItem {
        require(isMenu || isRoot)
        return UiModelItem(ModelType.MENU, name, label, "", VarType.STR).also {
            addNode(model, it, findNode(model, this)!!)
        }.apply(initializer)
    }

    fun createVariable(
        model: DefaultTreeModel,
        name: String,
        label: String,
        value: String,
        type: VarType
    ): UiModelItem {
        require(isMenu || isRoot)
        return UiModelItem(ModelType.VAR, name, label, value, type).also {
            addNode(model, it, findNode(model, this)!!)
        }
    }

    fun createAction(
        model: DefaultTreeModel,
        name: String,
        label: String,
        value: String,
    ): UiModelItem {
        require(isMenu || isRoot)
        return UiModelItem(ModelType.ACTION, name, label, value, VarType.STR).also {
            addNode(model, it, findNode(model, this)!!)
        }
    }

    fun getParent(model: DefaultTreeModel): UiModelItem =
        fromTreeNode((findNode(model, this) as MutableTreeNode).parent)

    fun removeFromParent(model: DefaultTreeModel) {
        model.removeNodeFromParent(findNode(model, this) as MutableTreeNode)
    }

    fun removeChild(model: DefaultTreeModel, uiItem: UiModelItem) {
        model.removeNodeFromParent(findNode(model, uiItem) as MutableTreeNode)
    }

    fun reload(model: DefaultTreeModel) {
        model.reload(findNode(model, this))
    }

    override fun equals(other: Any?): Boolean =
        if (this === other) true else if (other !is UiModelItem) false else name == other.name

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String =
        "UICheatItem(menuType=$menuType, name='$name', label='$label', value='$value', type=$type)"

    companion object {
        fun createRoot(initializer: UiModelItem.() -> Unit = { }): UiModelItem =
            UiModelItem(ModelType.ROOT, "", "", "", VarType.STR).apply(initializer)

        fun fromTreeNode(value: Any): UiModelItem = ((value as DefaultMutableTreeNode).userObject as UiModelItem)

        private fun addNode(model: DefaultTreeModel, node: UiModelItem, parent: TreeNode) {
            model.insertNodeInto(DefaultMutableTreeNode(node), parent as DefaultMutableTreeNode, parent.childCount)
            model.reload(parent)
        }

        fun findNode(model: DefaultTreeModel, criteria: (UiModelItem) -> Boolean): TreeNode? =
            (model.root as DefaultMutableTreeNode).depthFirstEnumeration().toList()
                .firstOrNull { criteria(fromTreeNode(it)) }

        fun findNode(model: DefaultTreeModel, item: UiModelItem): TreeNode? =
            findNode(model) { (it) == item }

        fun findNodes(model: DefaultTreeModel, criteria: (UiModelItem) -> Boolean): List<TreeNode> =
            (model.root as DefaultMutableTreeNode).depthFirstEnumeration().toList()
                .filter { criteria(fromTreeNode(it)) }

        fun findNodes(model: DefaultTreeModel, item: UiModelItem): List<TreeNode> =
            findNodes(model) { (it) == item }

        fun clear(model: DefaultTreeModel) {
            (model.root as DefaultMutableTreeNode).children().toList().forEach {
                model.removeNodeFromParent(it as MutableTreeNode)
            }
        }

    }
}
