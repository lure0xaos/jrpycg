package com.github.lure0xaos.util.log.jul

import com.github.lure0xaos.util.log.Level

object LevelConverter {
    fun level(level: Level): java.util.logging.Level =
        when (level) {
            Level.ERROR -> java.util.logging.Level.SEVERE
            Level.WARNING -> java.util.logging.Level.WARNING
            Level.INFO -> java.util.logging.Level.INFO
            Level.DEBUG -> java.util.logging.Level.FINE
            Level.TRACE -> java.util.logging.Level.FINER
        }

    fun level(level: java.util.logging.Level): Level =
        when (level) {
            java.util.logging.Level.SEVERE -> Level.ERROR
            java.util.logging.Level.WARNING -> Level.WARNING
            java.util.logging.Level.INFO -> Level.INFO
            java.util.logging.Level.FINE -> Level.DEBUG
            java.util.logging.Level.FINER -> Level.TRACE
            java.util.logging.Level.FINEST -> Level.TRACE
            else -> Level.TRACE
        }
}
