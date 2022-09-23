package com.github.lure0xaos.util.ui.main

import com.github.lure0xaos.util.Parameters
import com.github.lure0xaos.util.get
import com.github.lure0xaos.util.getResourceBundle
import com.github.lure0xaos.util.ui.dialog.confirm
import com.github.lure0xaos.util.ui.preloader.Preloader
import com.github.lure0xaos.util.ui.swing
import java.util.ResourceBundle
import java.util.prefs.Preferences
import javax.swing.JFrame

open class JExtFrame(
    protected val preloader: Preloader?,
    args: Array<String>
) : JFrame() {

    protected val preferences: Preferences = Preferences.userNodeForPackage(javaClass)
    protected val parameters: Parameters = Parameters(args)

    private val resources: ResourceBundle = JExtFrame::class.getResourceBundle(JExtFrame::class, locale)

    protected lateinit var callback: (returning: Return) -> Unit
    private var returning: Return = Return.SHUTDOWN

    fun doShow(callback: (returning: Return) -> Unit = { }) {
        this.callback = callback
        preloader?.notifyProgress(99)
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        preloader?.hide()
    }

    protected open fun onClose(): Boolean =
        confirm(resources[LC_CONFIRM_CLOSE]) == true

    protected fun doClose() {
        if (onClose()) {
            swing { callback(returning) }
            dispose()
        }
    }

    internal fun restart() {
        returning = Return.RESTART
        doClose()
    }

    companion object {
        private const val LC_CONFIRM_CLOSE = "confirm.close"
    }
}
