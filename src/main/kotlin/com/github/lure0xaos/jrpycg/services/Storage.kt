package com.github.lure0xaos.jrpycg.services

import com.github.lure0xaos.jrpycg.model.ModelItem
import java.nio.file.Path
import kotlin.io.path.*

class Storage {
    private lateinit var path: Path

    val isInitialized: Boolean
        get() = ::path.isInitialized

    fun saveAs(newPath: Path, root: ModelItem): Result<Unit> =
        doSave(newPath, root).onSuccess {
            path = newPath
        }

    fun save(root: ModelItem): Result<Unit> =
        doSave(path, root)

    fun reload(): Result<ModelItem> =
        doLoad(path)

    fun loadAs(newPath: Path): Result<ModelItem> =
        doLoad(newPath).onSuccess {
            path = newPath
        }

    companion object {
        private fun doSave(path: Path, root: ModelItem): Result<Unit> =
            runCatching {
                require(root.isRoot)
                if (path.exists()) path.deleteExisting()
                path.writeLines(ScriptConverter.toScript(root))
            }

        private fun doLoad(path: Path): Result<ModelItem> =
            runCatching {
                require(path.exists() && path.isRegularFile())
                ScriptConverter.fromScript(path.readLines())
            }.also { result ->
                result.getOrNull()?.let { require(it.isRoot) }
            }
    }
}
