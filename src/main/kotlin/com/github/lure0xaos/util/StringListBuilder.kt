package com.github.lure0xaos.util

class StringListBuilder private constructor() {
    private val buffer: MutableList<String> = mutableListOf()

    internal operator fun String.unaryPlus() {
        buffer.add(this)
    }

    internal operator fun List<String>.unaryPlus() {
        buffer.addAll(this)
    }

    private fun build(): List<String> = buffer

    companion object {
        fun buildStringList(action: StringListBuilder.() -> Unit): List<String> =
            StringListBuilder().apply(action).build()
    }
}
