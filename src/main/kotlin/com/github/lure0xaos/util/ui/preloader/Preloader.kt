package com.github.lure0xaos.util.ui.preloader

import com.github.lure0xaos.util.log.Log
import java.awt.Color
import java.awt.Container
import javax.swing.JWindow

open class Preloader(customizer: Container.() -> Unit) {
    private var disposed = false
    val window: JWindow = JWindow().apply {
        background = Color(0, 0, 0, 0)
        contentPane.apply {
            customizer()
            pack()
            setLocationRelativeTo(null)
        }
    }

    fun show(): Preloader =
        apply {
            if (!disposed) {
                window.isVisible = true
                notifyProgress(0)
            }
        }

    fun hide() {
        if (!disposed) {
            disposed = true
            notifyProgress(100)
            window.dispose()
        }
    }

    open fun notifyProgress(percent: Int = 100, message: String = "") {
        Log.info { "$percent% $message" }
    }
}
