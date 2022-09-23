package com.github.lure0xaos.jrpycg.ui.comp.storage

import java.io.File
import java.nio.file.Path
import javax.swing.Icon
import javax.swing.filechooser.FileView

class RPyCGFileView(private val iconProvider: (Path) -> Icon?) : FileView() {
    override fun getIcon(f: File?): Icon? =
        runCatching { f?.toPath() }.getOrNull()?.let(iconProvider) ?: super.getIcon(f)
}
