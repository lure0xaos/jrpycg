package com.github.lure0xaos.util.ui.dialog

import com.github.lure0xaos.util.getResourceBundle
import java.util.ResourceBundle
import javax.swing.JButton

object ButtonFactory {
    fun createButton(buttonType: ButtonType): JButton =
        JButton(buttonType.text, buttonType.icon)

    val bundle: ResourceBundle =
        ButtonFactory::class.getResourceBundle(ButtonFactory::class)

}
