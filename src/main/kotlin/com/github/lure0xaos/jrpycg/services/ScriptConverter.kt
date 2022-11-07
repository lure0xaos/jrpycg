package com.github.lure0xaos.jrpycg.services

import com.github.lure0xaos.jrpycg.model.ModelItem
import com.github.lure0xaos.jrpycg.model.VarType

object ScriptConverter {

    fun toScript(item: ModelItem): List<String> =
        when {
            item.isVariable() -> listOf(buildString {
                append(item.name)
                if (item.value.isNotBlank()) append("=${item.value}")
                append("(${item.type.keyword})")
                if (item.label.isNotBlank() && item.label != item.name) append(";${item.label}")
            })

            item.isRoot() -> item.children.flatMap { toScript(it) }
            item.isMenu() ->
                listOf("<${item.name};${item.label}") +
                        item.children.flatMap { toScript(it) } +
                        listOf(">")

            else -> error("")
        }

    fun fromScript(lines: List<String>): ModelItem {
        val root = ModelItem.createRoot()
        var item: ModelItem = root
        for (line in lines) line.trim().let {
            if (it == ">") item = item.parent ?: return root
            else if (it.startsWith('<')) item = it.removePrefix("<").let {
                item.createMenu(it.substringBefore(';'), it.substringAfter(';'))
            } else if (it.isNotBlank()) {
                Regex("^([^=(]+)(?:=(.+))?\\((.+)\\)(?:;(.+))?$").find(it)?.groupValues?.let { values ->
                    item.createVariable(values[1], values[4], values[2], VarType.find(values[3]))
                } ?: error(it)
            }
        }
        return root
    }

}
