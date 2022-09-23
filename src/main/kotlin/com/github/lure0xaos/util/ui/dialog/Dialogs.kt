package com.github.lure0xaos.util.ui.dialog

import com.github.lure0xaos.util.ui.GridBagConstraints
import com.github.lure0xaos.util.ui.JPanel
import com.github.lure0xaos.util.ui.findWindow
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dialog
import java.awt.Frame
import java.awt.GridBagLayout
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.SwingConstants

fun Component.alert(
    message: String,
    icon: Icon? = null,
    title: String? = null,
    dialogType: DialogType = DialogType.OK,
    modalityType: Dialog.ModalityType = Dialog.ModalityType.APPLICATION_MODAL,
    factory: (ButtonType) -> JButton = ButtonFactory::createButton,
    panelCustomizer: JPanel.() -> Unit = {},
    dialogCustomizer: UIDialog<JPanel, ButtonType>.() -> Unit = {}
) {
    UIDialog(
        this, findTitle(title), modalityType, createPanel(message, icon, dialogType, panelCustomizer), dialogType,
        { it }, Closing.DISPOSE, factory
    ).apply { dialogCustomizer() }.showAndWait()
}

fun Component.alertError(
    error: Throwable,
    message: String = error.localizedMessage,
    icon: Icon? = null,
    title: String? = null,
    dialogType: DialogType = DialogType.OK,
    modalityType: Dialog.ModalityType = Dialog.ModalityType.APPLICATION_MODAL,
    factory: (ButtonType) -> JButton = ButtonFactory::createButton,
    panelCustomizer: JPanel.() -> Unit = {},
    dialogCustomizer: UIDialog<JPanel, ButtonType>.() -> Unit = {}
) {
    UIDialog(this, findTitle(title), modalityType,
        createPanel(message, icon ?: DialogIcon.ERROR.icon, dialogType) {
            add(JScrollPane(JTextArea(error.stackTraceToString()).apply {
                isOpaque = false
                isEditable = false
            }), GridBagConstraints {
                ipadx = 20
                ipady = 20
                gridy = 1
            })
            panelCustomizer()
        }, dialogType, { it }, Closing.DISPOSE, factory
    ).apply { dialogCustomizer() }.showAndWait()
}

fun Component.confirm(
    message: String,
    icon: Icon? = null,
    title: String? = null,
    dialogType: DialogType = DialogType.OK_CANCEL,
    modalityType: Dialog.ModalityType = Dialog.ModalityType.APPLICATION_MODAL,
    factory: (ButtonType) -> JButton = ButtonFactory::createButton,
    panelCustomizer: JPanel.() -> Unit = {},
    dialogCustomizer: UIDialog<JPanel, Boolean>.() -> Unit = {}
): Boolean? =
    UIDialog(
        this, findTitle(title), modalityType, createPanel(message, icon, dialogType, panelCustomizer), dialogType,
        { it.isDefault }, Closing.DISPOSE, factory
    ).apply { dialogCustomizer() }.showAndWait()

fun Component.prompt(
    message: String,
    icon: Icon? = null,
    defaultText: String = "",
    title: String? = null,
    dialogType: DialogType = DialogType.OK_CANCEL,
    modalityType: Dialog.ModalityType = Dialog.ModalityType.APPLICATION_MODAL,
    factory: (ButtonType) -> JButton = ButtonFactory::createButton,
    panelCustomizer: JPanel.() -> Unit = {},
    dialogCustomizer: UIDialog<JPanel, String>.() -> Unit = {}
): String? {
    val input = JTextField(defaultText)
    return UIDialog(
        this, findTitle(title), modalityType, createPanel(message, icon, dialogType) {
            add(input, BorderLayout.SOUTH)
            panelCustomizer()
        }, dialogType, { if (!it.isCancel) input.text else null }, Closing.DISPOSE, factory
    ).apply { dialogCustomizer() }.showAndWait()
}

fun Component.findTitle(title: String?): String =
    title ?: findWindow().let {
        when (it) {
            is Frame -> it.title
            is Dialog -> it.title
            else -> ""
        }
    }

private fun createPanel(
    message: String,
    icon: Icon?,
    dialogType: DialogType,
    customizer: JPanel.() -> Unit = {}
): JPanel =
    JPanel(GridBagLayout()) {
        add(JLabel(message, icon ?: dialogType.icon, SwingConstants.CENTER), GridBagConstraints {
            ipadx = 20
            ipady = 20
        })
        customizer()
    }
