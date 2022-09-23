package com.github.lure0xaos.util.ui.dialog

import com.github.lure0xaos.util.getResource
import com.github.lure0xaos.util.toIcon
import javax.swing.Icon

enum class DialogIcon(iconName: String) {
    INFO("info.gif"),
    CONFIRM("confirm.gif"),
    WARN("warn.gif"),
    ERROR("error.gif");

    val icon: Icon = DialogIcon::class.getResource(iconName).toIcon()
}

enum class DialogType(val icon: Icon, vararg buttonTypes: ButtonType) {
    OK(DialogIcon.INFO.icon, ButtonType.OK),
    OK_CANCEL(DialogIcon.CONFIRM.icon, ButtonType.OK, ButtonType.CANCEL),
    YES_NO(DialogIcon.CONFIRM.icon, ButtonType.YES, ButtonType.NO),
    YES_NO_CANCEL(DialogIcon.CONFIRM.icon, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

    val buttonTypes: Array<ButtonType> = buttonTypes.asList().toTypedArray()

}
