package com.github.lure0xaos.jrpycg.ui.comp.builder

import com.github.lure0xaos.jrpycg.model.ModelItem
import com.github.lure0xaos.jrpycg.model.VarType
import com.github.lure0xaos.jrpycg.res.Res
import com.github.lure0xaos.jrpycg.res.icons.ResIcon
import com.github.lure0xaos.jrpycg.services.LocaleHolder
import com.github.lure0xaos.jrpycg.services.ModelConverter
import com.github.lure0xaos.jrpycg.ui.model.UiModelItem
import com.github.lure0xaos.jrpycg.ui.model.UiModelItem.Companion.fromTreeNode
import com.github.lure0xaos.util.get
import com.github.lure0xaos.util.getResourceBundle
import com.github.lure0xaos.util.ui.JMenuItem
import com.github.lure0xaos.util.ui.dialog.Closing
import com.github.lure0xaos.util.ui.dialog.DialogType
import com.github.lure0xaos.util.ui.dialog.UIDialog
import com.github.lure0xaos.util.ui.isRightMouseButton
import com.github.lure0xaos.util.ui.render.DecoratedTreeCellRenderer
import com.github.lure0xaos.util.ui.swing
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dialog
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.ResourceBundle
import javax.swing.DropMode
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel


class BuilderPanel(private val localeHolder: LocaleHolder) : JPanel(BorderLayout()) {
    private val resources: ResourceBundle = Res::class.getResourceBundle(BuilderPanel::class, localeHolder.locale)

    private val treeModel: DefaultTreeModel = DefaultTreeModel(DefaultMutableTreeNode(UiModelItem.createRoot()))
    private val popupMenu = JPopupMenu()

    private val tree: JTree = JTree(treeModel).apply {
        isRootVisible = false
        cellRenderer = DecoratedTreeCellRenderer<DefaultMutableTreeNode> { _, value, _, _, _, _, _ ->
            isOpaque = true
            val item = fromTreeNode(value)
            text = item.let { it.label.ifEmpty { it.name } }
            icon = item.let {
                when {
                    it.isRoot() -> ResIcon.COMPUTER.icon
                    it.isMenu() -> ResIcon.MENU.icon
                    it.isVariable() -> ResIcon.VARIABLE.icon
                    else -> null
                }
            }
            background = colorItem(value.childCount, value.parent?.childCount ?: 0)
        }
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.isRightMouseButton) {
                    setSelectionRow(getClosestRowForLocation(e.x, e.y))
                    updatePopup(fromTreeNode((selectionModel.selectionPath).lastPathComponent))
                    popupMenu.show(e.component, e.x, e.y)
                }
            }
        })
        dragEnabled = true
        dropMode = DropMode.ON_OR_INSERT
        transferHandler = TreeTransferHandler()
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
    }

    private fun colorItem(childCount: Int, siblingCount: Int): Color {
        return when (childCount) {
            in (0..MAX_ITEM_WARN) -> when (siblingCount) {
                in (0..MAX_ITEM_WARN) -> COLOR_ITEM_NORMAL
                in (MAX_ITEM_WARN..MAX_ITEM_DANGER) -> COLOR_ITEM_WARN
                else -> COLOR_ITEM_DANGER
            }

            in (MAX_ITEM_WARN..MAX_ITEM_DANGER) -> COLOR_ITEM_WARN
            else -> COLOR_ITEM_DANGER
        }
    }

    fun getModel(): ModelItem = ModelConverter.toModel(treeModel)

    fun setUiRoot(root: ModelItem) {
        ModelConverter.toModel(treeModel, root).also {
            swing {
                treeModel.setRoot(it)
                treeModel.reload(it)
            }
        }
    }

    private fun updatePopup(uiItem: UiModelItem) {
        popupMenu.removeAll()
        if (uiItem.isVariable()) {
            popupMenu.add(JMenuItem(resources[LC_POPUP_VARIABLE_EDIT]) {
                onEditItem(uiItem)
            })
            popupMenu.add(JMenuItem(resources[LC_POPUP_VARIABLE_DELETE]) {
                onDeleteItem(uiItem)
            })
        }
        if (uiItem.isMenu()) {
            popupMenu.add(JMenuItem(resources[LC_POPUP_MENU_EDIT]) {
                onEditMenu(uiItem)
            })
            popupMenu.add(JMenuItem(resources[LC_POPUP_MENU_DELETE]) {
                onDeleteItem(uiItem)
            })
            popupMenu.add(JMenuItem(resources[LC_POPUP_MENU_CREATE]) {
                onNewMenu { nameValue: String, labelValue: String ->
                    uiItem.createMenu(treeModel, nameValue, labelValue)
                }
            })
            popupMenu.add(JMenuItem(resources[LC_POPUP_VARIABLE_CREATE]) {
                onNewItem { nameValue: String, labelValue: String, valueValue: String, typeValue: VarType ->
                    uiItem.createVariable(treeModel, nameValue, labelValue, valueValue, typeValue)
                }
            })
        }
    }

    val callbackNewRootMenu: (String, String) -> UiModelItem = { nameValue: String, labelValue: String ->
        fromTreeNode(treeModel.root).createMenu(treeModel, nameValue, labelValue)
    }
    val callbackNewRootItem: (String, String, String, VarType) -> UiModelItem =
        { nameValue: String, labelValue: String, valueValue: String, typeValue: VarType ->
            fromTreeNode(treeModel.root).createVariable(treeModel, nameValue, labelValue, valueValue, typeValue)
        }

    fun isEmpty(): Boolean =
        (treeModel.root as DefaultMutableTreeNode).childCount == 0

    private fun onDeleteItem(uiItem: UiModelItem) {
        uiItem.removeFromParent(treeModel)
    }

    private fun onEditMenu(item: UiModelItem) {
        val menuPanel = CheatMenuPanel(localeHolder, item.name, item.label).apply {
            setPanelPreferredSize(this)
        }
        UIDialog(
            this, resources[LC_DIALOG_MENU_EDIT], Dialog.ModalityType.APPLICATION_MODAL, menuPanel, DialogType
                .OK_CANCEL, {
                if (it.isCancel) null else menuPanel
            }, { panel -> panel.validateItem() + checkDuplicate(panel) }, Closing.DISPOSE
        ).showAndWait()?.also {
            swing {
                item.name = it.nameValue
                item.label = it.labelValue
                item.reload(treeModel)
            }
        }
    }

    private fun onEditItem(item: UiModelItem) {
        val menuItemPanel = CheatVariablePanel(localeHolder, item.name, item.label, item.value, item.type).apply {
            setPanelPreferredSize(this)
        }
        UIDialog(
            this, resources[LC_DIALOG_VARIABLE_EDIT], Dialog.ModalityType.APPLICATION_MODAL, menuItemPanel, DialogType
                .OK_CANCEL, {
                if (it.isCancel) null else menuItemPanel
            }, { panel -> panel.validateItem() + checkDuplicate(panel) }, Closing.DISPOSE
        ).showAndWait()?.also {
            swing {
                item.name = it.nameValue
                item.label = it.labelValue
                item.value = it.valueValue
                item.type = it.typeValue
                item.reload(treeModel)
            }
        }
    }

    fun onNewMenu(callback: (nameValue: String, labelValue: String) -> Unit) {
        val menuPanel = CheatMenuPanel(localeHolder).apply {
            setPanelPreferredSize(this)
        }
        UIDialog(
            this,
            resources[LC_DIALOG_MENU_CREATE],
            Dialog.ModalityType.APPLICATION_MODAL,
            menuPanel,
            DialogType.OK_CANCEL,
            {
                if (it.isCancel) null else menuPanel
            },
            { panel -> panel.validateItem() + checkDuplicate(panel) }, Closing.DISPOSE
        ).showAndWait()?.also {
            callback(it.nameValue, it.labelValue)
        }
    }

    fun onNewItem(callback: (nameValue: String, labelValue: String, valueValue: String, typeValue: VarType) -> Unit) {
        val menuItemPanel = CheatVariablePanel(localeHolder).apply {
            setPanelPreferredSize(this)
        }
        UIDialog(
            this,
            resources[LC_DIALOG_VARIABLE_CREATE],
            Dialog.ModalityType.APPLICATION_MODAL,
            menuItemPanel,
            DialogType.OK_CANCEL,
            {
                if (it.isCancel) null else menuItemPanel
            },
            { panel -> panel.validateItem() + checkDuplicate(panel) }, Closing.DISPOSE
        ).showAndWait()?.also {
            callback(it.nameValue, it.labelValue, it.valueValue, it.typeValue)
        }
    }

    private fun checkDuplicate(panel: CheatMenuPanel) =
        if (UiModelItem.findNode(treeModel) { it.name == panel.nameValue } == null) emptyList()
        else listOf<Pair<List<String>, JComponent>>(
            listOf(resources[LC_VALIDATION_ITEM_DUPLICATE]) to panel
        )

    private fun checkDuplicate(panel: CheatVariablePanel) =
        if (UiModelItem.findNode(treeModel) { it.name == panel.nameValue } == null) emptyList()
        else listOf<Pair<List<String>, JComponent>>(
            listOf(resources[LC_VALIDATION_ITEM_DUPLICATE]) to panel
        )

    private fun setPanelPreferredSize(panel: JPanel) {
        panel.preferredSize = size?.let { Dimension((it.width * 0.75).toInt(), (it.height * 0.5).toInt()) }
    }

    init {
        apply {
            add(JScrollPane(tree).apply {
            }, BorderLayout.CENTER)
        }
    }

    companion object {
        private val COLOR_ITEM_NORMAL: Color = Color(0xffffff)
        private val COLOR_ITEM_WARN: Color = Color(0xffff80)
        private val COLOR_ITEM_DANGER: Color = Color(0xff8080)
        private const val MAX_ITEM_DANGER = 10
        private const val MAX_ITEM_WARN = 8

        private const val LC_POPUP_VARIABLE_EDIT = "popup.variable.edit"
        private const val LC_POPUP_VARIABLE_DELETE = "popup.variable.delete"
        private const val LC_POPUP_MENU_EDIT = "popup.menu.edit"
        private const val LC_POPUP_MENU_DELETE = "popup.menu.delete"
        private const val LC_POPUP_MENU_CREATE = "popup.menu.create"
        private const val LC_POPUP_VARIABLE_CREATE = "popup.variable.create"
        private const val LC_DIALOG_MENU_EDIT = "dialog.menu.edit"
        private const val LC_DIALOG_VARIABLE_EDIT = "dialog.variable.edit"
        private const val LC_DIALOG_MENU_CREATE = "dialog.menu.create"
        private const val LC_DIALOG_VARIABLE_CREATE = "dialog.variable.create"
        private const val LC_VALIDATION_ITEM_DUPLICATE = "validation.item.duplicate"
    }
}
