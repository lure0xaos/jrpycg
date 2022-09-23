package com.github.lure0xaos.util.log.jul

import com.github.lure0xaos.util.log.ILogger
import com.github.lure0xaos.util.log.ILoggerFactory
import com.github.lure0xaos.util.log.LoggerFactory
import java.util.logging.LogManager

internal class JULLoggerFactory : ILoggerFactory {
    override fun getLogger(name: String): ILogger = JULLogger(name)

    init {
        init()
    }

    companion object {
        private const val CONFIG = "/logging.properties"
        private fun init() {
            LoggerFactory::class.java.getResourceAsStream(CONFIG)?.runCatching {
                use { LogManager.getLogManager().readConfiguration(it) }
            }?.onFailure { logError("logger configuration error from $CONFIG") }
                ?.onSuccess { JULLogger("").info { "logger configured successfully from $CONFIG" } }
                ?: logError("logger configuration not found at $CONFIG")
        }

        private fun logError(message: String): Unit = System.err.println(message)
    }
}
