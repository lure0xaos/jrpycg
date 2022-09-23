package com.github.lure0xaos.util.log

fun interface ILoggerFactory {
    fun getLogger(name: String): ILogger
}
