package com.github.lure0xaos.util.ui.dialog

import com.github.lure0xaos.util.ui.JButtonPanel
import com.github.lure0xaos.util.ui.findTitle
import com.github.lure0xaos.util.ui.findWindow
import com.github.lure0xaos.util.ui.swing
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dialog
import java.awt.Frame
import java.awt.Insets
import java.awt.Window
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.KeyEvent
import java.util.concurrent.Semaphore
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.KeyStroke
import javax.swing.border.Border

class UIDialog<C : JComponent, R : Any>(
    owner: Component,
    title: String = owner.findTitle(null),
    modality: ModalityType,
    content: C,
    dialogType: DialogType = DialogType.OK,
    val resultConverter: (ButtonType) -> R?,
    validator: (C) -> List<Pair<List<String>, JComponent>> = { emptyList() },
    private val onClose: Closing,
    private val factory: (ButtonType) -> JButton = ButtonFactory::createButton
) : JDialog(
    owner.findWindow().let {
        when (it) {
            is Dialog -> it
            is Frame -> it
            else -> error("wrong parent")
        }
    }, title, modality
) {
    constructor(
        owner: Component,
        title: String = owner.findTitle(null),
        modality: ModalityType,
        content: C,
        dialogType: DialogType = DialogType.OK,
        resultConverter: (ButtonType) -> R?,
        onClose: Closing
    ) : this(
        owner, title, modality, content, dialogType, resultConverter,
        { emptyList<Pair<List<String>, JComponent>>() }, onClose
    )

    constructor(
        owner: Component,
        title: String = owner.findTitle(null),
        modality: ModalityType,
        content: C,
        dialogType: DialogType = DialogType.OK,
        resultConverter: (ButtonType) -> R?,
        onClose: Closing,
        factory: (ButtonType) -> JButton = ButtonFactory::createButton
    ) : this(
        owner, title, modality, content, dialogType, resultConverter,
        { emptyList<Pair<List<String>, JComponent>>() }, onClose, factory
    )

    constructor(
        owner: Component,
        title: String = owner.findTitle(null),
        modality: ModalityType,
        content: C,
        dialogType: DialogType = DialogType.OK,
        resultConverter: (ButtonType) -> R?
    ) : this(
        owner, title, modality, content, dialogType, resultConverter, { emptyList<Pair<List<String>, JComponent>>() },
        Closing.DISPOSE
    )

    @Volatile
    private var result: R? = null
    private val semaphore: Semaphore = Semaphore(1)
    private lateinit var button: ButtonType

    var onShow: () -> Unit = {}
    var onHide: () -> Unit = {}

    private fun onAction(buttonType: ButtonType) {
        button = buttonType
        result = resultConverter(button)
        semaphore.release()
        when (onClose) {
            Closing.HIDE -> isVisible = false
            Closing.DISPOSE -> dispose()
        }
    }

    fun showDialog() {
        result = null
        pack()
        setLocationRelativeTo(owner)
        isVisible = true
    }

    fun showAndWait(): R? {
        showDialog()
        semaphore.acquire()
        return result
    }

    init {
        addComponentListener(object : ComponentAdapter() {
            override fun componentShown(e: ComponentEvent?) {
                onShow()
            }

            override fun componentHidden(e: ComponentEvent?) {
                onHide()
            }
        })
        parent.also { if (it is Window) iconImages = it.iconImages }
        defaultCloseOperation = when (onClose) {
            Closing.HIDE -> HIDE_ON_CLOSE
            Closing.DISPOSE -> DISPOSE_ON_CLOSE
        }
        contentPane.apply {
            layout = BorderLayout()
            add(content, BorderLayout.CENTER)
            add(JButtonPanel(dialogType.buttonTypes.map { buttonType: ButtonType ->
                (factory(buttonType).apply {
                    margin = Insets(0, 0, 0, 0)
                    toolTipText = text
                    addActionListener {
                        validator(content).let { errors: List<Pair<List<String>, JComponent>> ->
                            errors to (buttonType.isCancel || errors.isEmpty()).let { noErrors: Boolean ->
                                decorateErrors(noErrors, errors)
                                if (noErrors)
                                    onAction(buttonType)
                            }
                        }
                    }
                    swing {
                        if (buttonType.isDefault) this@UIDialog.rootPane.defaultButton = this
                        if (buttonType.isCancel) this@UIDialog.rootPane.registerKeyboardAction(
                            { onAction(buttonType) },
                            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                            JComponent.WHEN_IN_FOCUSED_WINDOW
                        )
                    }
                })
            }) {
                border = BorderFactory.createEtchedBorder()
            }, BorderLayout.SOUTH)
        }
        pack()
        setLocationRelativeTo(owner)
    }

    private val states: MutableMap<JComponent, Pair<Border, String>> = mutableMapOf()
    private fun decorateErrors(noErrors: Boolean, errors: List<Pair<List<String>, JComponent>>) {
        val defaultButton: JButton? = this@UIDialog.rootPane.defaultButton
        if (noErrors) {
            states.forEach { (component: JComponent, state: Pair<Border, String>) ->
                component.border = state.first
                component.toolTipText = state.second
            }
            defaultButton?.border = BorderFactory.createEmptyBorder()
        } else {
            errors.forEach { (err, component): Pair<List<String>, JComponent> ->
                if (component !in states) states[component] = component.border to component.toolTipText
                component.border = BorderFactory.createLineBorder(Color.RED)
                component.toolTipText = err.joinToString("\n")
            }
            defaultButton?.border = BorderFactory.createLineBorder(Color.RED)
        }
    }

}
