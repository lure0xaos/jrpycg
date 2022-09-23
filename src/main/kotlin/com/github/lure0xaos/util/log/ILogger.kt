package com.github.lure0xaos.util.log

interface ILogger {
    fun error(message: () -> String): Unit = log(Level.ERROR, null, message)
    fun error(e: Throwable, message: () -> String): Unit = log(Level.ERROR, e, message)
    fun warn(message: () -> String): Unit = log(Level.WARNING, null, message)
    fun warn(e: Throwable, message: () -> String): Unit = log(Level.WARNING, e, message)
    fun info(message: () -> String): Unit = log(Level.INFO, null, message)
    fun info(e: Throwable, message: () -> String): Unit = log(Level.INFO, e, message)
    fun debug(message: () -> String): Unit = log(Level.DEBUG, null, message)
    fun debug(e: Throwable, message: () -> String): Unit = log(Level.DEBUG, e, message)
    fun trace(message: () -> String): Unit = log(Level.TRACE, null, message)
    fun trace(e: Throwable, message: () -> String): Unit = log(Level.TRACE, e, message)
    fun log(level: Level, e: Throwable? = null, message: () -> String): Unit
    fun isErrorEnabled(level: Level): Boolean = isEnabled(Level.ERROR)
    fun isWarningEnabled(level: Level): Boolean = isEnabled(Level.WARNING)
    fun isInfoEnabled(level: Level): Boolean = isEnabled(Level.INFO)
    fun isDebugEnabled(level: Level): Boolean = isEnabled(Level.DEBUG)
    fun isTraceEnabled(level: Level): Boolean = isEnabled(Level.TRACE)
    fun isEnabled(level: Level): Boolean
}
