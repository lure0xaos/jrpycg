package com.github.lure0xaos.util.ui

import java.awt.AWTKeyStroke
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.Frame
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.GridBagConstraints
import java.awt.Image
import java.awt.Insets
import java.awt.LayoutManager
import java.awt.Point
import java.awt.RenderingHints
import java.awt.Window
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.font.FontRenderContext
import java.awt.geom.Dimension2D
import java.awt.geom.Point2D
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.text.ParseException
import javax.swing.DefaultListModel
import javax.swing.DefaultListSelectionModel
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JFormattedTextField
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JTextField
import javax.swing.KeyStroke
import javax.swing.ListModel
import javax.swing.SwingUtilities
import javax.swing.text.JTextComponent
import javax.swing.text.MaskFormatter
import kotlin.reflect.KClass

fun swing(action: () -> Unit): Unit = SwingUtilities.invokeLater(action)

fun Component.findWindow(): Window? = if (this is Window) this else SwingUtilities.getWindowAncestor(this)

val MouseEvent.isRightMouseButton: Boolean
    get() = SwingUtilities.isRightMouseButton(this)

inline fun JPanel(manager: LayoutManager, crossinline customizer: JPanel.() -> Unit): JPanel =
    JPanel(manager).apply { customizer() }

fun JButton(text: String, actionListener: ActionListener): JButton =
    JButton(text).apply { addActionListener(actionListener) }

fun JButton(text: String, icon: Icon, actionListener: ActionListener): JButton =
    JButton(text, icon).apply { addActionListener(actionListener) }

fun JPopupMenu(vararg items: JMenuItem, customizer: JPopupMenu.() -> Unit = {}): JPopupMenu =
    JPopupMenu().apply {
        items.forEach { add(it) }
        customizer()
    }

fun JMenuItem(text: String, actionListener: ActionListener): JMenuItem =
    JMenuItem(text).apply { addActionListener(actionListener) }

fun JMenuItem(text: String, icon: Icon, actionListener: ActionListener): JMenuItem =
    JMenuItem(text, icon).apply { addActionListener(actionListener) }

fun String.escapeHtml(): String =
    this
        .replace("&", "&alt;")
        .replace("\"", "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

fun String.urlEncode(charset: Charset = Charsets.UTF_8): String =
    URLEncoder.encode(this, charset)

fun String.urlDecode(charset: Charset = Charsets.UTF_8): String =
    URLDecoder.decode(this, charset)


fun JFormattedTextField.isFormattedValid(): Boolean {
    try {
        commitEdit()
    } catch (e: ParseException) {
        return false
    }
    return true
}

fun JFormattedTextField.isMaskedEmpty(): Boolean {
    if (formatter !is MaskFormatter) return false
    val maskFormatter = formatter as MaskFormatter
    return text == maskFormatter.mask.replace('#', maskFormatter.placeholderCharacter)
}

fun <T : JTextComponent> T.makeSelectable(): T =
    apply {
        addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent) {
                selectAll()
            }
        })
    }


val monoFont: Font =
    FontRenderContext(null, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF, RenderingHints.VALUE_FRACTIONALMETRICS_OFF)
        .let { context: FontRenderContext ->
            GraphicsEnvironment.getLocalGraphicsEnvironment().allFonts.first { font: Font ->
                font.family == Font.MONOSPACED &&
                        font.isPlain && font.getStringBounds(I, context).width == font.getStringBounds(W, context).width
            }
        }

@Suppress("UNCHECKED_CAST")
inline fun <reified W : Frame> JComponent.findFrame(clazz: KClass<W> = (JFrame::class as KClass<W>)): W? =
    SwingUtilities.getAncestorOfClass(clazz.java, this) as W?

val JComponent.graphics2d: Graphics2D
    get() = graphics as Graphics2D

fun Graphics.as2D(): Graphics2D = this as Graphics2D

operator fun <E> ListModel<E>.get(index: Int): E = getElementAt(index)

operator fun Dimension2D.component1(): Double = width
operator fun Dimension2D.component2(): Double = height

operator fun Dimension.component1(): Int = width
operator fun Dimension.component2(): Int = height

operator fun Point2D.component1(): Double = x
operator fun Point2D.component2(): Double = y

operator fun Point.component1(): Int = x
operator fun Point.component2(): Int = y

operator fun Point2D.Float.component1(): Float = x
operator fun Point2D.Float.component2(): Float = y

operator fun Dimension.invoke(width: Int, height: Int): Unit = setSize(width, height)

val Image.width: Int get() = getWidth(null)

val Image.height: Int get() = getHeight(null)

val Image.size: Dimension
    get() = Dimension(width, height)

val InputEvent.isLeftMouseButtonDown: Boolean get() = (modifiersEx and InputEvent.BUTTON1_DOWN_MASK) != 0

val InputEvent.isMiddleMouseButtonDown: Boolean get() = (modifiersEx and InputEvent.BUTTON2_DOWN_MASK) != 0

val InputEvent.isRightMouseButtonDown: Boolean get() = (modifiersEx and InputEvent.BUTTON3_DOWN_MASK) != 0

fun InputEvent.isMouseButtonDown(button: Int): Boolean = (modifiersEx and InputEvent.getMaskForButton(button)) != 0

val MouseEvent.isLeftButton: Boolean get() = button == MouseEvent.BUTTON1

val MouseEvent.isMiddleButton: Boolean get() = button == MouseEvent.BUTTON2

val MouseEvent.isRightButton: Boolean get() = button == MouseEvent.BUTTON3

val KeyEvent.keyText: String get() = KeyEvent.getKeyText(keyCode)

val KeyEvent.keyStroke: KeyStroke get() = KeyStroke.getKeyStrokeForEvent(this)

val AWTKeyStroke.isAltDown: Boolean get() = (modifiers and InputEvent.ALT_DOWN_MASK) != 0

val AWTKeyStroke.isShiftDown: Boolean get() = (modifiers and InputEvent.SHIFT_DOWN_MASK) != 0

val AWTKeyStroke.isControlDown: Boolean get() = (modifiers and InputEvent.CTRL_DOWN_MASK) != 0

val AWTKeyStroke.isMetaDown: Boolean get() = (modifiers and InputEvent.META_DOWN_MASK) != 0

val AWTKeyStroke.isAltGraphDown: Boolean get() = (modifiers and InputEvent.ALT_GRAPH_DOWN_MASK) != 0

inline operator fun <reified R : Any> JComponent.get(name: String): R? = getClientProperty(name) as R?

operator fun <R> JComponent.set(name: String, value: R): Unit = putClientProperty(name, value)

operator fun JPanel.set(comp: JComponent, constraints: Any): Unit = add(comp, constraints)

fun swing(action: Runnable): Unit = SwingUtilities.invokeLater(action)


fun JLabel.html(html: String, base: URL) {
    text = U_0000
    this["html.base"] = base
    text = html
}

fun JLabel(html: String, base: URL): JLabel =
    JLabel().apply {
        text = U_0000
        this["html.base"] = base
        text = html
    }

fun JButton(text: String, icon: Icon, action: (ActionEvent) -> Unit): JButton =
    JButton(BaseAction(text, icon, action))

fun JButton(icon: Icon, action: (ActionEvent) -> Unit): JButton =
    JButton(BaseAction("", icon, action))

fun JButton(text: String, action: (ActionEvent) -> Unit): JButton =
    JButton(BaseAction(text, action))

fun Insets(top: Int, left: Int, bottom: Int, right: Int, customizer: Insets.() -> Unit): Insets =
    Insets(top, left, bottom, right).apply(customizer)

fun Insets(vertical: Int, horizontal: Int, customizer: Insets.() -> Unit = {}): Insets =
    Insets(vertical, horizontal, vertical, horizontal).apply(customizer)

fun Insets(gap: Int, customizer: Insets.() -> Unit = {}): Insets =
    Insets(gap, gap, gap, gap).apply(customizer)

fun GridBagConstraints(customizer: GridBagConstraints.() -> Unit = {}): GridBagConstraints =
    GridBagConstraints().apply(customizer)

fun JTextField.selectableField(): JTextField =
    apply {
        addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent) {
                selectAll()
            }
        })
    }

fun JButton.setAsDefault() {
    SwingUtilities.getRootPane(this)?.defaultButton = this
}

fun <E : Any> DefaultListModel(customizer: DefaultListModel<E>.() -> Unit): DefaultListModel<E> =
    DefaultListModel<E>().apply(customizer)

fun DefaultListSelectionModel(customizer: DefaultListSelectionModel.() -> Unit): DefaultListSelectionModel =
    DefaultListSelectionModel().apply(customizer)

fun <E : Any> DefaultListModel<E>.setAll(items: Collection<E>) {
    clear()
    addAll(items)
}

private const val U_0000 = "\u0000"
private const val I = "i"
private const val W = "w"
