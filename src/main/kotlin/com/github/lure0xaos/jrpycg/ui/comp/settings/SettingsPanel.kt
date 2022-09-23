package com.github.lure0xaos.jrpycg.ui.comp.settings

import com.github.lure0xaos.jrpycg.model.Settings
import com.github.lure0xaos.jrpycg.res.Res
import com.github.lure0xaos.jrpycg.res.flags.Flag
import com.github.lure0xaos.jrpycg.res.icons.ResIcon
import com.github.lure0xaos.jrpycg.services.CodeGenerator
import com.github.lure0xaos.jrpycg.services.LocaleHolder
import com.github.lure0xaos.util.findResourceBundleLocales
import com.github.lure0xaos.util.get
import com.github.lure0xaos.util.getResourceBundle
import com.github.lure0xaos.util.resolveName
import com.github.lure0xaos.util.ui.JButton
import com.github.lure0xaos.util.ui.JFormPanel
import com.github.lure0xaos.util.ui.JPanel
import com.github.lure0xaos.util.ui.dialog.ButtonType
import com.github.lure0xaos.util.ui.dialog.confirm
import com.github.lure0xaos.util.ui.key.JKeyButton
import com.github.lure0xaos.util.ui.key.KeyCombination
import com.github.lure0xaos.util.ui.render.DecoratedListCellRenderer
import java.awt.BorderLayout
import java.awt.GridLayout
import java.util.Locale
import java.util.ResourceBundle
import java.util.prefs.Preferences
import javax.swing.BorderFactory
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListCellRenderer
import javax.swing.border.TitledBorder

class SettingsPanel(
    private val localeHolder: LocaleHolder,
    private val preferences: Preferences,
    val settings: Settings,
    private val restarter: () -> Unit = {}
) : JPanel(BorderLayout()) {
    private val resources: ResourceBundle = Res::class.getResourceBundle(SettingsPanel::class, localeHolder.locale)

    private val chkEnableCheat: JCheckBox = JCheckBox()
    private val chkEnableConsole: JCheckBox = JCheckBox()
    private val chkEnableDeveloper: JCheckBox = JCheckBox()
    private val chkEnableRollback: JCheckBox = JCheckBox()
    private val chkEnableWrite: JCheckBox = JCheckBox()

    private val cmbLocaleMenu: JComboBox<Locale> = JComboBox(CodeGenerator.locales).apply {
        renderer = getLocaleListCellRenderer(localeHolder.locale)
    }
    private val cmbLocaleUi: JComboBox<Locale> = JComboBox(
        Settings::class.findResourceBundleLocales(SettingsPanel::class.resolveName(), Locale.ENGLISH).toTypedArray()
    ).apply {
        renderer = getLocaleListCellRenderer(localeHolder.locale)
    }.shouldWarnRestart()
    private val keyCheat: JKeyButton = JKeyButton(KeyCombination(), localeHolder.locale)
    private val keyConsole: JKeyButton = JKeyButton(KeyCombination(), localeHolder.locale)
    private val keyDeveloper: JKeyButton = JKeyButton(KeyCombination(), localeHolder.locale)
    private val keyWrite: JKeyButton = JKeyButton(KeyCombination(), localeHolder.locale)

    private fun getLocaleListCellRenderer(locale: Locale = Locale.getDefault()): ListCellRenderer<Any> =
        DecoratedListCellRenderer<Locale> { _, value, _, _, _ ->
            text = "${value.getDisplayName(Locale.ROOT)} (${value.getDisplayName(locale)})"
            icon = Flag.findFlag(value.language)
        }

    private fun update(newSettings: Settings) {
        cmbLocaleMenu.selectedItem = newSettings.localeMenu
        chkEnableRollback.isSelected = newSettings.enableRollback
        chkEnableCheat.isSelected = newSettings.enableCheat
        chkEnableConsole.isSelected = newSettings.enableConsole
        chkEnableDeveloper.isSelected = newSettings.enableDeveloper
        chkEnableWrite.isSelected = newSettings.enableWrite
        keyCheat.keyCombination = newSettings.keyCheat
        keyConsole.keyCombination = newSettings.keyConsole
        keyDeveloper.keyCombination = newSettings.keyDeveloper
        keyWrite.keyCombination = newSettings.keyWrite
        warningRestart = false
    }

    private fun updateSettings() {
        settings.setAll(
            chkEnableCheat.isSelected,
            chkEnableConsole.isSelected,
            chkEnableDeveloper.isSelected,
            chkEnableWrite.isSelected,
            chkEnableRollback.isSelected,
            keyCheat.keyCombination,
            keyConsole.keyCombination,
            keyDeveloper.keyCombination,
            keyWrite.keyCombination,
            cmbLocaleMenu.selectedItem as Locale
        )
        localeHolder.locale = cmbLocaleUi.selectedItem as Locale
    }

    private fun reset() {
        update(Settings.fromDefaults(preferences))
        updateSettings()
        cmbLocaleUi.selectedItem = localeHolder.locale
    }

    private fun update() {
        update(settings)
        cmbLocaleUi.selectedItem = localeHolder.locale
        warningRestart = false
    }

    fun persist() {
        updateSettings()
        localeHolder.locale = cmbLocaleUi.selectedItem as Locale
    }


    private fun JComponent.setTitled(title: String) {
        border =
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP
            )
    }

    init {
        add(JPanel(GridLayout(1, 2)) {
            add(JFormPanel {
                setTitled(resources[LC_OPTIONS_TITLE])
                addLabelControl(resources[LC_CHEAT], chkEnableCheat)
                addLabelControl(resources[LC_CONSOLE], chkEnableConsole)
                addLabelControl(resources[LC_DEVELOPER], chkEnableDeveloper)
                addLabelControl(resources[LC_ROLLBACK], chkEnableRollback)
                addLabelControl(resources[LC_WRITE], chkEnableWrite)
            })
            add(JFormPanel {
                setTitled(resources[LC_OPTIONS_KEYS])
                addLabelControl(resources[LC_KEY_CHEAT], keyCheat)
                addLabelControl(resources[LC_KEY_CONSOLE], keyConsole)
                addLabelControl(resources[LC_KEY_DEVELOPER], keyDeveloper)
                addLabelControl(resources[LC_KEY_WRITE], keyWrite)
                addLabelControl("", JButton(resources[LC_OPTIONS_RESET], ResIcon.RESET.icon) {
                    reset()
                })
            })
        }, BorderLayout.CENTER)
        add(JFormPanel {
            setTitled(resources[LC_LANGUAGE_TITLE])
            addLabelControl(resources[LC_LANGUAGE_UI], cmbLocaleUi)
            addLabelControl(resources[LC_LANGUAGE_MENU], cmbLocaleMenu)
        }, BorderLayout.SOUTH)
        update(Settings.fromDefaults(preferences))
    }

    @Volatile
    private var warningRestart: Boolean = false
    private fun <E> JComboBox<E>.shouldWarnRestart(): JComboBox<E> =
        apply {
            addItemListener { warningRestart = true }
        }


    private fun warnRestart() {
        if (warningRestart) {
            if (confirm(
                    resources[LC_NEED_RESTART],
                    factory = { buttonType ->
                        when (buttonType) {
                            ButtonType.OK -> javax.swing.JButton(resources[LC_NEED_RESTART_OK])
                            ButtonType.CANCEL -> javax.swing.JButton(resources[LC_NEED_RESTART_CANCEL])
                            else -> error("")
                        }
                    }
                ) == true
            ) restarter()
        }
        warningRestart = false
    }

    fun onShow() {
        update()
    }

    fun onHide() {
        persist()
        warnRestart()
    }

    companion object {
        private const val LC_OPTIONS_TITLE = "options.title"
        private const val LC_OPTIONS_KEYS = "options.keys"
        private const val LC_LANGUAGE_TITLE = "language.title"
        private const val LC_CHEAT = "cheat"
        private const val LC_CONSOLE = "console"
        private const val LC_DEVELOPER = "developer"
        private const val LC_ROLLBACK = "rollback"
        private const val LC_WRITE = "write"
        private const val LC_KEY_CHEAT = "key_cheat"
        private const val LC_KEY_CONSOLE = "key_console"
        private const val LC_KEY_DEVELOPER = "key_developer"
        private const val LC_KEY_WRITE = "key_write"
        private const val LC_OPTIONS_RESET = "options.reset"
        private const val LC_LANGUAGE_UI = "language.ui"
        private const val LC_LANGUAGE_MENU = "language.menu"

        private const val LC_NEED_RESTART = "need-restart"
        private const val LC_NEED_RESTART_CANCEL = "need-restart-cancel"
        private const val LC_NEED_RESTART_OK = "need-restart-ok"
    }
}
