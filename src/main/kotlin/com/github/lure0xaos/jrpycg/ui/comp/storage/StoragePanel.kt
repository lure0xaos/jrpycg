package com.github.lure0xaos.jrpycg.ui.comp.storage

import com.github.lure0xaos.jrpycg.model.ModelItem
import com.github.lure0xaos.jrpycg.res.Res
import com.github.lure0xaos.jrpycg.res.icons.ResIcon
import com.github.lure0xaos.jrpycg.services.LocaleHolder
import com.github.lure0xaos.jrpycg.services.Storage
import com.github.lure0xaos.util.USER_HOME
import com.github.lure0xaos.util.get
import com.github.lure0xaos.util.getResourceBundle
import com.github.lure0xaos.util.pref.set
import java.awt.Component
import java.nio.file.Path
import java.util.*
import java.util.prefs.Preferences
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile


class StoragePanel(localeHolder: LocaleHolder, private val preferences: Preferences) {
    private val resources: ResourceBundle = Res::class.getResourceBundle(StoragePanel::class, localeHolder.locale)

    private val storage: Storage = Storage()

    private var storageDirectory: Path
        get() = Path(preferences[PREF_STORAGE, USER_HOME.toString()])
        set(storageDirectory) {
            preferences[PREF_STORAGE] = storageDirectory.toFile().absolutePath
        }

    private val chooser: JFileChooser = JFileChooser().apply {
        currentDirectory = storageDirectory.toFile()
        fileSelectionMode = JFileChooser.FILES_ONLY
        fileFilter = FileNameExtensionFilter(resources[LC_FILTER_DESCRIPTION], EXT)
        fileView = RPyCGFileView { if (it.isRegularFile() && it.fileName.extension == EXT) ResIcon.FILE.icon else null }
    }

    val isInitialized: Boolean
        get() = storage.isInitialized

    fun loadAs(path: Path): Result<ModelItem> = storage.loadAs(path)
    fun reload(): Result<ModelItem> = storage.reload()
    fun save(root: ModelItem): Result<Unit> = storage.save(root)
    fun saveAs(path: Path, root: ModelItem): Result<Unit> = storage.saveAs(path, root)


    fun selectLoadFile(parent: Component): Path? =
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
            chooser.selectedFile.toPath().let { ensureExtension(it) }.also { storageDirectory = it.parent }
        else null

    fun selectSaveFile(parent: Component): Path? =
        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION)
            chooser.selectedFile.toPath().let { ensureExtension(it) }.also { storageDirectory = it.parent }
        else null

    private fun ensureExtension(path: Path): Path =
        if (path.fileName.extension == EXT) path else path.resolveSibling("${path.fileName}.$EXT")

    companion object {
        private const val LC_FILTER_DESCRIPTION = "filter.description"

        private const val EXT = "rpycg"
        private const val PREF_STORAGE = "storage"
    }
}
