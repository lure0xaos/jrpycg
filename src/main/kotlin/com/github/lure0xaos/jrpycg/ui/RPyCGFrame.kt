package com.github.lure0xaos.jrpycg.ui

import com.github.lure0xaos.jrpycg.model.Settings
import com.github.lure0xaos.jrpycg.model.VarType
import com.github.lure0xaos.jrpycg.res.Res
import com.github.lure0xaos.jrpycg.res.icons.ResIcon
import com.github.lure0xaos.jrpycg.services.*
import com.github.lure0xaos.jrpycg.ui.comp.about.AboutPanel
import com.github.lure0xaos.jrpycg.ui.comp.builder.BuilderPanel
import com.github.lure0xaos.jrpycg.ui.comp.creator.CreatorPanel
import com.github.lure0xaos.jrpycg.ui.comp.settings.SettingsPanel
import com.github.lure0xaos.jrpycg.ui.comp.storage.StoragePanel
import com.github.lure0xaos.jrpycg.ui.comp.system.SystemPanel
import com.github.lure0xaos.util.*
import com.github.lure0xaos.util.pref.set
import com.github.lure0xaos.util.ui.*
import com.github.lure0xaos.util.ui.dc.JDirectoryChooser
import com.github.lure0xaos.util.ui.dialog.alert
import com.github.lure0xaos.util.ui.dialog.confirm
import com.github.lure0xaos.util.ui.main.JExtFrame
import com.github.lure0xaos.util.ui.preloader.Preloader
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.nio.file.Path
import java.util.*
import javax.swing.JMenuItem
import javax.swing.JTabbedPane
import javax.swing.SwingConstants
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.isDirectory
import kotlin.io.path.writeLines

class RPyCGFrame(preloader: Preloader?, args: Array<String>) : JExtFrame(preloader, args) {

    private val settings: Settings = Settings.fromDefaults(preferences)

    private val localeHolder: LocaleHolder = LocaleHolder(preferences)

    private val resources: ResourceBundle = Res::class.getResourceBundle(RPyCGFrame::class, localeHolder.locale)

    private val email = Email(localeHolder)

    private val storagePanel: StoragePanel = StoragePanel(localeHolder, preferences)

    private val tabs: JTabbedPane
    private val builder: BuilderPanel
    private val creator: CreatorPanel
    private val settingsPanel: SettingsPanel
    private val about: AboutPanel

    private val btnSave: JMenuItem
    private val btnReload: JMenuItem


    private fun toGameDirectory(path: Path): Path {
        require(isGameDirectory(path))
        return path / GAME / RPYCG_CHEAT_RPY
    }

    private fun isGameDirectory(path: Path): Boolean =
        path.isDirectory() && ((path / RENPY).isDirectory()) && ((path / GAME).isDirectory())


    private fun updateButtons() {
        swing {
            btnReload.isEnabled = storagePanel.isInitialized
            btnSave.isEnabled = storagePanel.isInitialized
        }
    }

    private fun onNewMenu() {
        builder.onNewMenu { nameValue: String, labelValue: String ->
            builder.callbackNewRootMenu(nameValue, labelValue)
            creator.callbackNewRootMenu(nameValue, labelValue)
        }
    }

    private fun onNewItem() {
        builder.onNewItem { nameValue: String, labelValue: String, valueValue: String, typeValue: VarType ->
            builder.callbackNewRootItem(nameValue, labelValue, valueValue, typeValue)
            creator.callbackNewRootItem(nameValue, labelValue, valueValue, typeValue)
        }
    }

    private fun onNewAction() {
        builder.onNewAction { nameValue: String, labelValue: String, valueValue: String ->
            builder.callbackNewRootAction(nameValue, labelValue, valueValue)
            creator.callbackNewRootAction(nameValue, labelValue, valueValue)
        }
    }

    private fun onTab() {
        if (isActiveSettings) {
            settingsPanel.onShow()
        } else {
            settingsPanel.onHide()
            if (isActiveBuilder) {
                runCatching {
                    if (creator.isChanged) builder.setUiRoot(ScriptConverter.fromScript(creator.script))
                    creator.isChanged = false
                }
            }
            if (isActiveCreator) {
                runCatching {
                    creator.script = ScriptConverter.toScript(builder.getModel())
                    creator.isChanged = false
                    creator.onShow()
                }
            }
        }
    }

    private val isActiveAbout: Boolean
        get() = tabs.selectedComponent === about

    private val isActiveSettings: Boolean
        get() = tabs.selectedComponent === settingsPanel

    private val isActiveCreator: Boolean
        get() = tabs.selectedComponent === creator

    private val isActiveBuilder: Boolean
        get() = tabs.selectedComponent === builder

    private var gameDirectory: Path
        get() = Path(preferences[PREF_GAME, USER_HOME.toString()])
        set(gameDirectory) {
            preferences[PREF_GAME] = gameDirectory.toFile().absolutePath
        }

    private val chooser = JDirectoryChooser(
        customizer = {
            preferredSize = Dimension(this@RPyCGFrame.width / 2, this@RPyCGFrame.height)
        },
        decorator = { _, value, _, expanded, _, _, _ ->
            val path = value.directory
            icon = if (path.isDirectory()) {
                if (isGameDirectory(path)) {
                    if (expanded) ResIcon.GAME_FOLDER_OPEN.icon else ResIcon.GAME_FOLDER.icon
                } else {
                    if (expanded) ResIcon.FOLDER_OPEN.icon else ResIcon.FOLDER.icon
                }
            } else {
                ResIcon.FILE.icon
            }
        }).apply {
        selectedDirectory = gameDirectory
    }

    private fun canGenerate(): Boolean =
        (!isActiveCreator || !creator.isChanged || runCatching {
            if (creator.isChanged) builder.setUiRoot(ScriptConverter.fromScript(creator.script))
        }.onSuccess {
            creator.isChanged = false
        }.isSuccess && creator.checkErrors().isSuccess).also {
            settingsPanel.persist()
        }

    init {
        Email.install(this, localeHolder) {}
        glassPane = JImagePanel(
            RPyCGFrame::class.getResource(RPyCGFrame::class.resolveName() + EXT_JPG).toImage(),
            fits = Fit.FITS.MAX, anchorVertical = 1.0
        )
        glassPane.isVisible = true
        title = resources[LC_TITLE]
        iconImage = RPyCGFrame::class.findResource(RPyCGFrame::class.resolveName() + EXT_PNG)?.toImage()
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                doClose()
            }
        })
        contentPane.apply {
            layout = BorderLayout()
            add(JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT).also { tabs = it }.apply {
                addTab(
                    resources[LC_TAB_BUILDER],
                    ResIcon.BUILDER.icon,
                    BuilderPanel(localeHolder).also { builder = it })
                addTab(
                    resources[LC_TAB_CREATOR],
                    ResIcon.CREATOR.icon,
                    CreatorPanel(localeHolder).also { creator = it })
                addTab(
                    resources[LC_TAB_SETTINGS],
                    ResIcon.SETTINGS.icon,
                    SettingsPanel(localeHolder, preferences, settings, this@RPyCGFrame::restart).also {
                        settingsPanel = it
                    })
                addTab(resources[LC_TAB_ABOUT], ResIcon.ABOUT.icon, AboutPanel(localeHolder).also { about = it })
                if (parameters.contains("system")) {
                    addTab(resources[LC_TAB_SYSTEM], null, SystemPanel(localeHolder))
                }
                addChangeListener {
                    onTab()
                }
            }, BorderLayout.CENTER)
            add(JPanel(GridLayout(2, 1)) {
                add(JSizingLabel(resources[LC_TITLE], SwingConstants.CENTER).apply {
                })
                add(JButtonPanel(
                    listOf(
                        JButton(resources[LC_TOP_NEW_MENU], ResIcon.MENU.icon) { onNewMenu() },
                        JButton(resources[LC_TOP_NEW_VARIABLE], ResIcon.VARIABLE.icon) { onNewItem() },
                        JButton(resources[LC_TOP_NEW_ACTION], ResIcon.VARIABLE.icon) { onNewAction() }
                    )
                ))
            }, BorderLayout.NORTH)
            add(
                JButtonPanel(
                    JDropdownButton(
                        resources[LC_BOTTOM_DD_LOAD],
                        JMenuItem(resources[LC_BOTTOM_LOAD]) {
                            storagePanel.selectLoadFile(this)?.also { path ->
                                storagePanel.loadAs(path)
                                    .onSuccess {
                                        builder.setUiRoot(it)
                                        creator.script = ScriptConverter.toScript(it)
                                        updateButtons()
                                    }
                                    .onFailure { error ->
                                        updateButtons()
                                        email.askAndEmail(this@RPyCGFrame, resources[LC_ERROR_LOAD], error)
                                    }
                            }
                        },
                        JMenuItem(resources[LC_BOTTOM_RELOAD]) {
                            storagePanel.reload()
                                .onSuccess {
                                    builder.setUiRoot(it)
                                    creator.script = ScriptConverter.toScript(it)
                                    updateButtons()
                                }
                                .onFailure { error ->
                                    updateButtons()
                                    email.askAndEmail(this@RPyCGFrame, resources[LC_ERROR_LOAD], error)
                                }
                        }.also { btnReload = it }
                    ),
                    JButton(resources[LC_BOTTOM_CREATE_TEMPLATE], ResIcon.TEMPLATE.icon) {
                        if (builder.isEmpty() || this@RPyCGFrame.confirm(resources[LC_CONFIRM_LOSE]) == true) {
                            TemplateGenerator.createTemplate().also {
                                builder.setUiRoot(it)
                                creator.script = ScriptConverter.toScript(it)
                            }
                        }
                    },
                    JDropdownButton(
                        resources[LC_BOTTOM_DD_SAVE], ResIcon.SAVE.icon,
                        JMenuItem(resources[LC_BOTTOM_SAVE]) {
                            storagePanel.save(builder.getModel())
                                .onSuccess {
                                    updateButtons()
                                }
                                .onFailure { error ->
                                    updateButtons()
                                    email.askAndEmail(this@RPyCGFrame, resources[LC_ERROR_SAVE], error)
                                }
                        }.also { btnSave = it },
                        JMenuItem(resources[LC_BOTTOM_SAVE_AS]) {
                            storagePanel.selectSaveFile(this)?.also { path ->
                                storagePanel.saveAs(path, builder.getModel())
                                    .onSuccess {
                                        updateButtons()
                                    }
                                    .onFailure { error ->
                                        updateButtons()
                                        email.askAndEmail(this@RPyCGFrame, resources[LC_ERROR_SAVE], error)
                                    }
                            }
                        }
                    ),
                    JDropdownButton(resources[LC_BOTTOM_GENERATE], ResIcon.GENERATE.icon,
                        JMenuItem(resources[LC_BOTTOM_GENERATE_CLIPBOARD]) {
                            if (canGenerate()) {
                                putClipboard(
                                    CodeGenerator(settingsPanel.settings).generate(builder.getModel())
                                        .joinToString("\n")
                                )
                                this@RPyCGFrame.alert(resources[LC_SUCCESS_GENERATE_CLIPBOARD])
                            } else {
                                email.askAndEmail(
                                    this@RPyCGFrame,
                                    resources[LC_ERROR_SCRIPT],
                                    creator.checkErrors().exceptionOrNull()!!
                                )
                            }
                        },
                        JMenuItem(resources[LC_BOTTOM_GENERATE_FILE]) {
                            if (canGenerate()) {
                                chooser.selectedDirectory = gameDirectory
                                chooser.showDialog(
                                    this@RPyCGFrame,
                                    resources[LC_TITLE_GENERATE_FILE]
                                ) { isGameDirectory(it) }
                                    ?.let { toGameDirectory(it) }?.also {
                                        it.writeLines(
                                            CodeGenerator(settingsPanel.settings).generate(builder.getModel())
                                        )
                                    }?.also {
                                        gameDirectory = it.parent.parent
                                        this@RPyCGFrame.alert(resources[LC_SUCCESS_GENERATE_FILE])
                                    }
                            } else {
                                email.askAndEmail(
                                    this@RPyCGFrame,
                                    resources[LC_ERROR_SCRIPT],
                                    creator.checkErrors().exceptionOrNull()!!
                                )
                            }
                        }
                    )
                ), BorderLayout.SOUTH
            )
        }
        updateButtons()
    }

    companion object {
        private const val EXT_PNG = ".png"
        private const val EXT_JPG = ".jpg"

        private const val GAME = "game"
        private const val RENPY = "renpy"
        private const val RPYCG_CHEAT_RPY = "RPyCGCheat.rpy"
        private const val PREF_GAME = "game"

        private const val LC_TITLE = "title"
        private const val LC_TAB_BUILDER = "tab.builder"
        private const val LC_TAB_CREATOR = "tab.creator"
        private const val LC_TAB_SETTINGS = "tab.settings"
        private const val LC_TAB_ABOUT = "tab.about"
        private const val LC_TAB_SYSTEM = "tab.system"
        private const val LC_TOP_NEW_MENU = "top.new_menu"
        private const val LC_TOP_NEW_VARIABLE = "top.new_item"
        private const val LC_TOP_NEW_ACTION = "top.new_action"
        private const val LC_BOTTOM_DD_LOAD = "bottom.dd.load"
        private const val LC_BOTTOM_LOAD = "bottom.load"
        private const val LC_BOTTOM_CREATE_TEMPLATE = "bottom.create_template"
        private const val LC_BOTTOM_RELOAD = "bottom.reload"
        private const val LC_BOTTOM_DD_SAVE = "bottom.dd.save"
        private const val LC_BOTTOM_SAVE = "bottom.save"
        private const val LC_BOTTOM_SAVE_AS = "bottom.save_as"
        private const val LC_BOTTOM_GENERATE = "bottom.generate"
        private const val LC_BOTTOM_GENERATE_CLIPBOARD = "bottom.generate_clipboard"
        private const val LC_BOTTOM_GENERATE_FILE = "bottom.generate_file"
        private const val LC_TITLE_GENERATE_FILE = "title.generate_file"
        private const val LC_CONFIRM_LOSE = "confirm.lose"
        private const val LC_CONFIRM_CLOSE = "confirm.close"
        private const val LC_ERROR_LOAD = "error.load"
        private const val LC_ERROR_SAVE = "error.save"
        private const val LC_ERROR_SCRIPT = "error.script"
        private const val LC_SUCCESS_GENERATE_CLIPBOARD = "success.generate_clipboard"
        private const val LC_SUCCESS_GENERATE_FILE = "success.generate_file"
    }

}
