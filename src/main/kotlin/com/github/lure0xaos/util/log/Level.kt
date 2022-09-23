package com.github.lure0xaos.util.log

enum class Level(val text: String) {
    ERROR("ERROR"),
    WARNING("WARN"),
    INFO("INFO"),
    DEBUG("DEBUG"),
    TRACE("TRACE");

    val string: String by lazy { text.take(len).padStart(len) }

    companion object {
        private val len: Int by lazy { values().map { it }.maxOf { it.text.length } }
    }
}
