package com.github.lure0xaos.jrpycg.ui.comp.builder

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import javax.swing.tree.DefaultMutableTreeNode

class NodesTransferable(
    private val nodes: Array<DefaultMutableTreeNode>,
    private val flavors: Array<DataFlavor>,
    private val nodesFlavor: DataFlavor
) : Transferable {
    override fun getTransferData(flavor: DataFlavor): Any =
        nodes.also { require(isDataFlavorSupported(flavor)) { flavor } }

    override fun getTransferDataFlavors(): Array<DataFlavor> = flavors

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = nodesFlavor.equals(flavor)
}
