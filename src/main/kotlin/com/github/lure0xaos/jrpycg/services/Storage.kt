package com.github.lure0xaos.jrpycg.services

import com.github.lure0xaos.jrpycg.model.ModelItem
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

class Storage {
    private lateinit var path: Path

    fun isInitialized(): Boolean =
        ::path.isInitialized

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
                require(root.isRoot())
                require(!path.exists() || path.isRegularFile())
                path.writeLines(ScriptConverter.toScript(root))
            }

        private fun doLoad(path: Path): Result<ModelItem> =
            runCatching {
                require(path.exists() && path.isRegularFile())
                ScriptConverter.fromScript(path.readLines())
            }.also { result ->
                result.getOrNull()?.let { require(it.isRoot()) }
            }
    }
}
