package com.github.lure0xaos.util.ui.key

import java.awt.event.KeyEvent

data class KeyCombination(
    val key: Key = Key.VK_UNKNOWN,
    val isShiftDown: Boolean = false,
    val isControlDown: Boolean = false,
    val isMetaDown: Boolean = false,
    val isAltDown: Boolean = false,
    val isAltGraphDown: Boolean = false
) {
    constructor(event: KeyEvent) : this(
        key = Key.findByCode(event.keyCode),
        isControlDown = event.isControlDown,
        isShiftDown = event.isShiftDown,
        isAltDown = event.isAltDown,
        isMetaDown = event.isMetaDown,
        isAltGraphDown = event.isAltGraphDown
    )

    constructor() : this(
        key = Key.VK_UNKNOWN,
        isControlDown = false,
        isShiftDown = false,
        isAltDown = false,
        isMetaDown = false,
        isAltGraphDown = false
    )

    fun getString(): String =
        if (key.isUnknown) "" else buildString {
            if (isControlDown) append("Ctrl+")
            if (isShiftDown) append("Shift+")
            if (isAltDown) append("Alt+")
            append(key.text)
        }

    fun toBinding(): String =
        if (key.isUnknown) "" else buildString {
            if (isControlDown) append("ctrl_")
            if (isShiftDown) append("shift_")
            if (isAltDown) append("alt_")
            append(key.constant)
        }

    override fun toString(): String = getString()

    companion object {
        fun parse(text: String): KeyCombination {
            val list = text.split('+').map { it.trim() }
            return KeyCombination(
                key = Key.findByName(list.last()),
                isControlDown = "Ctrl" in list,
                isShiftDown = "Shift" in list,
                isAltDown = "Alt" in list,
                isMetaDown = false,
                isAltGraphDown = false
            )
        }
    }
}
