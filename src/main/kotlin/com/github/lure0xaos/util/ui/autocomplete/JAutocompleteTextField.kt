package com.github.lure0xaos.util.ui.autocomplete

import com.github.lure0xaos.util.ui.render.DecoratedListCellRenderer
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.Locale
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


class JAutocompleteTextField(
    items: (String) -> List<String>,
    decorator: JLabel.(tree: JList<String>, value: String, index: Int, isSelected: Boolean, hasFocus: Boolean) -> Unit =
        { _: JList<String>, _: String, _: Int, _: Boolean, _: Boolean -> }
) : JTextField() {

    val model: DefaultComboBoxModel<String> = DefaultComboBoxModel<String>()
    val drop: JComboBox<String> = object : JComboBox<String>(model) {
        override fun getPreferredSize(): Dimension = Dimension(super.getPreferredSize().width, 0)
    }

    init {
        setAdjusting(drop, false)
        drop.selectedItem = null
        drop.addActionListener {
            if (!isAdjusting(drop)) {
                val selectedItem = drop.selectedItem
                if (selectedItem != null && selectedItem is String) text = selectedItem
            }
        }
        drop.renderer = DecoratedListCellRenderer(decorator)
        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                setAdjusting(drop, true)
                if (e.keyCode == KeyEvent.VK_SPACE && drop.isPopupVisible) {
                    e.keyCode = KeyEvent.VK_ENTER
                }
                if ((e.keyCode == KeyEvent.VK_ENTER) || (e.keyCode == KeyEvent.VK_UP) || (e.keyCode == KeyEvent.VK_DOWN)) {
                    e.source = drop
                    drop.dispatchEvent(e)
                    if (e.keyCode == KeyEvent.VK_ENTER) {
                        val selectedItem = drop.selectedItem
                        if (selectedItem != null && selectedItem is String) {
                            text = selectedItem
                            drop.isPopupVisible = false
                        }
                    }
                }
                if (e.keyCode == KeyEvent.VK_ESCAPE) drop.isPopupVisible = false
                setAdjusting(drop, false)
            }
        })
        document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = updateList()
            override fun removeUpdate(e: DocumentEvent) = updateList()
            override fun changedUpdate(e: DocumentEvent) = updateList()

            private fun updateList() {
                setAdjusting(drop, true)
                model.removeAllElements()
                val input = text
                if (input.isNotEmpty())
                    items(input).filter { startsWithIgnoreCase(it, input) }.forEach { model.addElement(it) }
                drop.hidePopup()
                drop.updateUI()
                drop.isPopupVisible = model.size != 0
                setAdjusting(drop, false)
            }
        })
        layout = BorderLayout()
        add(drop, BorderLayout.SOUTH)
    }

    private fun startsWithIgnoreCase(string: String, substring: String, locale: Locale = Locale.getDefault()) =
        string.lowercase(locale).startsWith(substring.lowercase(locale))

    private fun isAdjusting(jComboBox: JComboBox<String>): Boolean =
        if (jComboBox.getClientProperty(IS_ADJUSTING) is Boolean) {
            jComboBox.getClientProperty(IS_ADJUSTING) as Boolean
        } else false

    private fun setAdjusting(jComboBox: JComboBox<String>, adjusting: Boolean) =
        jComboBox.putClientProperty(IS_ADJUSTING, adjusting)

    companion object {
        private const val IS_ADJUSTING = "is_adjusting"
    }
}
