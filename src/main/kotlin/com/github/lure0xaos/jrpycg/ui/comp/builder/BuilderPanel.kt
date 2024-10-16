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
import java.util.*
import javax.swing.*
import javax.swing.tree.*


class BuilderPanel(private val localeHolder: LocaleHolder) : JPanel(BorderLayout()) {
    private val resources: ResourceBundle = Res::class.getResourceBundle(BuilderPanel::class, localeHolder.locale)

    private val treeModel: DefaultTreeModel = DefaultTreeModel(DefaultMutableTreeNode(UiModelItem.createRoot()))
    private val popupMenu = JPopupMenu().apply {
    }

    private val tree: JTree = JTree(treeModel).apply {
        isRootVisible = false
        foreground = Color.BLACK
        cellRenderer = DecoratedTreeCellRenderer<DefaultMutableTreeNode>({
            textSelectionColor = Color.BLACK.darker()
            backgroundSelectionColor = COLOR_ITEM_NORMAL.darker()
        }) { _, value, selected, _, _, _, _ ->
            isOpaque = true
            val item = fromTreeNode(value)
            text = item.let { it.label.ifEmpty { it.name } }
            icon = item.let {
                when {
                    it.isRoot -> ResIcon.COMPUTER.icon
                    it.isMenu -> ResIcon.MENU.icon
                    it.isVariable -> ResIcon.VARIABLE.icon
                    else -> null
                }
            }
            background = if (selected)
                colorItem(value.childCount, value.parent?.childCount ?: 0).darker()
            else
                colorItem(value.childCount, value.parent?.childCount ?: 0)
        }
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.isRightMouseButton) {
                    setSelectionRow(getClosestRowForLocation(e.x, e.y))
                    val selectionPath = selectionModel.selectionPath
                    if (selectionPath != null) {
                        updatePopup(fromTreeNode(selectionPath.lastPathComponent))
                        popupMenu.show(e.component, e.x, e.y)
                    }
                }
            }
        })
        dragEnabled = true
        dropMode = DropMode.ON_OR_INSERT
        transferHandler = TreeTransferHandler()
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
    }

    private fun colorItem(childCount: Int, siblingCount: Int): Color =
        when (childCount) {
            in (0..MAX_ITEM_WARN) -> when (siblingCount) {
                in (0..MAX_ITEM_WARN) -> COLOR_ITEM_NORMAL
                in (MAX_ITEM_WARN..MAX_ITEM_DANGER) -> COLOR_ITEM_WARN
                else -> COLOR_ITEM_DANGER
            }

            in (MAX_ITEM_WARN..MAX_ITEM_DANGER) -> COLOR_ITEM_WARN
            else -> COLOR_ITEM_DANGER
        }

    fun getModel(): ModelItem = ModelConverter.toModel(treeModel)

    fun setUiRoot(root: ModelItem) {
        ModelConverter.toModel(treeModel, root).also {
            swing {
                treeModel.setRoot(it)
                treeModel.reload(it)
                for (child in it.children()) {
                    expandPath(child)
                }
            }
        }
    }

    private fun updatePopup(uiItem: UiModelItem) {
        popupMenu.removeAll()
        when {
            uiItem.isVariable -> listOf(
                JMenuItem(resources[LC_POPUP_VARIABLE_EDIT]) {
                    onEditItem(uiItem)
                },
                JMenuItem(resources[LC_POPUP_VARIABLE_DELETE]) {
                    onDeleteItem(uiItem)
                })

            uiItem.isAction -> listOf(
                JMenuItem(resources[LC_POPUP_ACTION_EDIT]) {
                    onEditAction(uiItem)
                },
                JMenuItem(resources[LC_POPUP_ACTION_DELETE]) {
                    onDeleteItem(uiItem)
                })

            uiItem.isMenu -> listOf(
                JMenuItem(resources[LC_POPUP_MENU_EDIT]) {
                    onEditMenu(uiItem)
                },
                JMenuItem(resources[LC_POPUP_MENU_DELETE]) {
                    onDeleteItem(uiItem)
                },
                JMenuItem(resources[LC_POPUP_MENU_CREATE]) {
                    onNewMenu { nameValue: String, labelValue: String ->
                        uiItem.createMenu(treeModel, nameValue, labelValue)
                        expandNode(uiItem)
                    }
                },
                JMenuItem(resources[LC_POPUP_VARIABLE_CREATE]) {
                    onNewItem { nameValue: String, labelValue: String, valueValue: String, typeValue: VarType ->
                        uiItem.createVariable(treeModel, nameValue, labelValue, valueValue, typeValue)
                    }
                },
                JMenuItem(resources[LC_POPUP_ACTION_CREATE]) {
                    onNewAction { nameValue: String, labelValue: String, valueValue: String ->
                        uiItem.createAction(treeModel, nameValue, labelValue, valueValue)
                    }
                })

            else -> listOf()
        }.forEach {
            popupMenu.add(it.apply {
            })
        }
    }

    val callbackNewRootMenu: (String, String) -> UiModelItem =
        { nameValue: String, labelValue: String ->
            fromTreeNode(treeModel.root).createMenu(treeModel, nameValue, labelValue)
        }

    val callbackNewRootItem: (String, String, String, VarType) -> UiModelItem =
        { nameValue: String, labelValue: String, valueValue: String, typeValue: VarType ->
            fromTreeNode(treeModel.root).createVariable(treeModel, nameValue, labelValue, valueValue, typeValue)
        }

    val callbackNewRootAction: (String, String, String) -> UiModelItem =
        { nameValue: String, labelValue: String, valueValue: String ->
            fromTreeNode(treeModel.root).createAction(treeModel, nameValue, labelValue, valueValue)
        }

    fun isEmpty(): Boolean =
        (treeModel.root as DefaultMutableTreeNode).childCount == 0

    private fun expandNode(uiModelItem: UiModelItem) {
        UiModelItem.findNode(treeModel, uiModelItem)?.also { expandPath(it) }
    }

    private fun expandPath(node: TreeNode) {
        tree.expandPath(TreePath(treeModel.getPathToRoot(node)))
    }

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
            }, { panel -> panel.validateItem() + checkDuplicate(panel, item) }, Closing.DISPOSE
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
            }, { panel -> panel.validateItem() + checkDuplicate(panel, item) }, Closing.DISPOSE
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

    private fun onEditAction(item: UiModelItem) {
        val menuItemPanel = CheatActionPanel(localeHolder, item.name, item.label, item.value).apply {
            setPanelPreferredSize(this)
        }
        UIDialog(
            this, resources[LC_DIALOG_ACTION_EDIT], Dialog.ModalityType.APPLICATION_MODAL, menuItemPanel, DialogType
                .OK_CANCEL, {
                if (it.isCancel) null else menuItemPanel
            }, { panel -> panel.validateItem() + checkDuplicate(panel, item) }, Closing.DISPOSE
        ).showAndWait()?.also {
            swing {
                item.name = it.nameValue
                item.label = it.labelValue
                item.value = it.valueValue
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
            { panel -> panel.validateItem() + checkDuplicate(panel, null) }, Closing.DISPOSE
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
            { panel -> panel.validateItem() + checkDuplicate(panel, null) }, Closing.DISPOSE
        ).showAndWait()?.also {
            callback(it.nameValue, it.labelValue, it.valueValue, it.typeValue)
        }
    }

    fun onNewAction(callback: (nameValue: String, labelValue: String, valueValue: String) -> Unit) {
        val menuItemPanel = CheatActionPanel(localeHolder).apply {
            setPanelPreferredSize(this)
        }
        UIDialog(
            this,
            resources[LC_DIALOG_ACTION_CREATE],
            Dialog.ModalityType.APPLICATION_MODAL,
            menuItemPanel,
            DialogType.OK_CANCEL,
            {
                if (it.isCancel) null else menuItemPanel
            },
            { panel -> panel.validateItem() + checkDuplicate(panel, null) }, Closing.DISPOSE
        ).showAndWait()?.also {
            callback(it.nameValue, it.labelValue, it.valueValue)
        }
    }

    private fun checkDuplicate(panel: CheatMenuPanel, item: UiModelItem?): List<Pair<List<String>, JComponent>> =
        checkDuplicate(panel, item) { it.name == panel.nameValue }

    private fun checkDuplicate(panel: CheatVariablePanel, item: UiModelItem?): List<Pair<List<String>, JComponent>> =
        checkDuplicate(panel, item) { it.name == panel.nameValue }

    private fun checkDuplicate(panel: CheatActionPanel, item: UiModelItem?): List<Pair<List<String>, JComponent>> =
        checkDuplicate(panel, item) { it.name == panel.nameValue }

    private fun checkDuplicate(comp: JComponent, item: UiModelItem?, check: (UiModelItem) -> Boolean)
            : List<Pair<List<String>, JComponent>> =
        if (UiModelItem.findNodes(treeModel) { it != item && check(it) }.isEmpty()) emptyList()
        else listOf(listOf(resources[LC_VALIDATION_ITEM_DUPLICATE]) to comp)

    private fun setPanelPreferredSize(panel: JPanel) {
        panel.preferredSize = size?.let { Dimension((it.width * 0.75).toInt(), it.height / 2) }
    }

    init {
        add(JScrollPane(tree), BorderLayout.CENTER)
    }

    companion object {
        private val COLOR_ITEM_NORMAL: Color = Color(0xffffff)
        private val COLOR_ITEM_WARN: Color = Color(0xffff80)
        private val COLOR_ITEM_DANGER: Color = Color(0xff8080)
        private const val MAX_ITEM_DANGER = 10
        private const val MAX_ITEM_WARN = 8

        private const val LC_POPUP_VARIABLE_EDIT = "popup.variable.edit"
        private const val LC_POPUP_ACTION_EDIT = "popup.action.edit"
        private const val LC_POPUP_VARIABLE_DELETE = "popup.variable.delete"
        private const val LC_POPUP_ACTION_DELETE = "popup.action.delete"
        private const val LC_POPUP_MENU_EDIT = "popup.menu.edit"
        private const val LC_POPUP_MENU_DELETE = "popup.menu.delete"
        private const val LC_POPUP_MENU_CREATE = "popup.menu.create"
        private const val LC_POPUP_VARIABLE_CREATE = "popup.variable.create"
        private const val LC_POPUP_ACTION_CREATE = "popup.action.create"
        private const val LC_DIALOG_MENU_EDIT = "dialog.menu.edit"
        private const val LC_DIALOG_VARIABLE_EDIT = "dialog.variable.edit"
        private const val LC_DIALOG_ACTION_EDIT = "dialog.action.edit"
        private const val LC_DIALOG_MENU_CREATE = "dialog.menu.create"
        private const val LC_DIALOG_VARIABLE_CREATE = "dialog.variable.create"
        private const val LC_DIALOG_ACTION_CREATE = "dialog.action.create"
        private const val LC_VALIDATION_ITEM_DUPLICATE = "validation.item.duplicate"
    }
}
