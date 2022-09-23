package com.github.lure0xaos.jrpycg.services

import com.github.lure0xaos.jrpycg.model.ModelItem
import com.github.lure0xaos.jrpycg.ui.model.UiModelItem
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

object ModelConverter {
    fun toModel(model: DefaultTreeModel): ModelItem =
        toModel(model, model.root as TreeNode, ModelItem.createRoot())

    private fun toModel(model: DefaultTreeModel, node: TreeNode, root: ModelItem): ModelItem =
        UiModelItem.fromTreeNode(node).let { item ->
            when {
                item.isVariable() -> root.createVariable(item.name, item.label, item.value, item.type)
                item.isMenu() -> root.createMenu(item.name, item.label).apply {
                    (0 until model.getChildCount(node)).map { model.getChild(node, it) }.forEach {
                        toModel(model, it as TreeNode, this)
                    }
                }

                item.isRoot() -> ModelItem.createRoot().apply {
                    (0 until model.getChildCount(node)).map { model.getChild(node, it) }.forEach {
                        toModel(model, it as TreeNode, this)
                    }
                }

                else -> error("")
            }
        }

    fun toModel(model: DefaultTreeModel, item: ModelItem): TreeNode {
        UiModelItem.clear(model)
        return toModel(model, item, UiModelItem.fromTreeNode(model.root))
    }

    private fun toModel(model: DefaultTreeModel, item: ModelItem, uiItem: UiModelItem): TreeNode =
        when {
            item.isVariable() -> {
                val uiCheatItem =
                    uiItem.createVariable(model, item.name, item.label, item.value, item.type)
                UiModelItem.findNode(model, uiCheatItem)!!
            }

            item.isMenu() -> {
                val uiCheatItem = uiItem.createMenu(model, item.name, item.label).apply {
                    item.children.forEach {
                        toModel(model, it, this)
                    }
                }
                UiModelItem.findNode(model, uiCheatItem)!!
            }

            item.isRoot() -> {
                UiModelItem.findNode(model, UiModelItem.createRoot().apply {
                    item.children.forEach {
                        toModel(model, it, this)
                    }
                })!!
            }

            else -> error("")
        }
}
