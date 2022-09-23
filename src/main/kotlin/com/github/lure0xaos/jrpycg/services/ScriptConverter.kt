package com.github.lure0xaos.jrpycg.services

import com.github.lure0xaos.jrpycg.model.ModelItem
import com.github.lure0xaos.jrpycg.model.VarType

object ScriptConverter {

    fun toScript(item: ModelItem): List<String> =
        when {
            item.isVariable -> listOf(buildString {
                append(item.name)
                if (item.value.isNotBlank()) append("=${item.value}")
                append("(${item.type.keyword})")
                if (item.label.isNotBlank() && item.label != item.name) append(";${item.label}")
            })

            item.isAction -> listOf(buildString {
                append(item.name)
                if (item.value.isNotBlank())
                    append("=${item.value}")
                append("(rpy)")
                if (item.label.isNotBlank() && item.label != item.name) append(";${item.label}")
            })

            item.isRoot -> item.children.flatMap { toScript(it) }
            item.isMenu ->
                listOf("<${item.name};${item.label}") +
                        item.children.flatMap { toScript(it) } +
                        listOf(">")

            else -> error("")
        }

    fun fromScript(lines: List<String>): ModelItem {
        val root = ModelItem.createRoot()
        var item: ModelItem = root
        lines.forEach { line ->
            line.trim().let {
                when {
                    it == ">" -> item = item.parent ?: return root
                    it.startsWith('<') -> item = it.removePrefix("<").let { line ->
                        item.createMenu(line.substringBefore(';'), line.substringAfter(';'))
                    }

                    it.isNotBlank() ->
                        Regex("^([^=(]+)(?:=(.+))?\\((.+)\\)(?:;(.+))?$").find(it)
                            ?.takeIf { it.groups.size == 5 }?.groupValues?.let { values ->
                                when {
                                    values[3] == "rpy" -> item.createAction(
                                        values[1],
                                        values[4],
                                        values[2]
                                    )

                                    else -> item.createVariable(
                                        values[1],
                                        values[4],
                                        values[2],
                                        VarType.find(values[3])
                                    )
                                }
                            } ?: error(it)
                }
            }
        }
        return root
    }

}
