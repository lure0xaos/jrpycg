package com.github.lure0xaos.util.ui

import java.awt.Dimension
import java.awt.Graphics
import java.awt.Image
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.Action
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import kotlin.math.max

class JDropdownButton : JButton {
    var popupMenu: JPopupMenu? = null
        set(value) {
            field?.selectionModel?.addChangeListener { e -> println(e) }
            field = value
        }

    var alwaysDropDown: Boolean = false
        set(value) {
            val changed = field != value
            if (changed) {
                if (value) addActionListener(defaultActionAction) else removeActionListener(defaultActionAction)
            }
            field = value
        }

    private val defaultActionAction: ActionListener = ActionListener {
        menuItems(popupMenu)?.firstOrNull()?.doClick()
    }
    private var inMenuLocation = false

    constructor(text: String, popupMenu: JPopupMenu, alwaysDropDown: Boolean = false) : super("$text ") {
        this.popupMenu = popupMenu
        this.alwaysDropDown = alwaysDropDown
    }

    constructor(text: String, icon: Icon, popupMenu: JPopupMenu, alwaysDropDown: Boolean = false) :
            super("$text ", icon) {
        this.popupMenu = popupMenu
        this.alwaysDropDown = alwaysDropDown
    }

    constructor(icon: Icon, popupMenu: JPopupMenu, alwaysDropDown: Boolean = false) : super(icon) {
        this.popupMenu = popupMenu
        this.alwaysDropDown = alwaysDropDown
    }

    constructor(action: Action, popupMenu: JPopupMenu, alwaysDropDown: Boolean = false) : super(action) {
        this.popupMenu = popupMenu
        this.alwaysDropDown = alwaysDropDown
    }

    constructor(text: String, vararg popupMenu: JMenuItem, alwaysDropDown: Boolean = false) : super("$text ") {
        this.popupMenu = JPopupMenu(*popupMenu)
        this.alwaysDropDown = alwaysDropDown
    }

    constructor(text: String, icon: Icon, vararg popupMenu: JMenuItem, alwaysDropDown: Boolean = false) :
            super("$text ", icon) {
        this.popupMenu = JPopupMenu(*popupMenu)
        this.alwaysDropDown = alwaysDropDown
    }

    constructor(icon: Icon, vararg popupMenu: JMenuItem, alwaysDropDown: Boolean = false) : super(icon) {
        this.popupMenu = JPopupMenu(*popupMenu)
        this.alwaysDropDown = alwaysDropDown
    }

    constructor(action: Action, vararg popupMenu: JMenuItem, alwaysDropDown: Boolean = false) : super(action) {
        this.popupMenu = JPopupMenu(*popupMenu)
        this.alwaysDropDown = alwaysDropDown
    }

    init {
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                if (alwaysDropDown || inMenuLocation || e.isPopupTrigger) showMenu(popupMenu)
            }

            override fun mouseReleased(e: MouseEvent) {
                if (e.isPopupTrigger) showMenu(popupMenu)
            }
        })
        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                inMenuLocation = e.point.x >= width - gap - gap - downArrowWidth
            }
        })
    }

    private fun showMenu(jPopupMenu: JPopupMenu?) {
        jPopupMenu?.let {
            it.preferredSize = Dimension(
                max(width, it.preferredSize.width),
                it.preferredSize.height
            )
            it.show(this, 0, height)
        }
    }

    private fun menuItems(jPopupMenu: JPopupMenu?): List<JMenuItem>? {
        return jPopupMenu?.let { (0 until jPopupMenu.componentCount) }
            ?.map { jPopupMenu.getComponent(it) }?.filterIsInstance<JMenuItem>()
    }

    override fun fireActionPerformed(event: ActionEvent) {
        if (!alwaysDropDown && !inMenuLocation) super.fireActionPerformed(event)
    }


    override fun paint(g: Graphics) {
        super.paint(g)
        g.drawImage(downArrow, width - downArrowWidth - gap, height / 2, null)
    }

    companion object {
        private const val gap = 10
        private val downArrow: Image = ImageIcon(JDropdownButton::class.java.getResource("down-arrow.png")).image
        private val downArrowWidth: Int = downArrow.getWidth(null)
    }
}
