package com.github.lure0xaos.util.ui.dialog

import com.github.lure0xaos.util.findResource
import com.github.lure0xaos.util.get
import javax.swing.ImageIcon

enum class ButtonType(val isDefault: Boolean, val isCancel: Boolean, textKey: String, iconKey: String) {
    OK(true, false, "ok.text", "ok.icon"),
    CANCEL(false, true, "cancel.text", "cancel.icon"),
    YES(true, false, "yes.text", "yes.icon"),
    NO(false, false, "no.text", "no.icon");

    val text: String = ButtonFactory.bundle[textKey]
    val icon: ImageIcon? = ButtonFactory.bundle[iconKey]
        .let { ButtonType::class.findResource(it) }
        ?.let { ImageIcon(it) }
}
