package com.github.lure0xaos.util.log.jul

import com.github.lure0xaos.util.log.ILogger
import com.github.lure0xaos.util.log.Level
import com.github.lure0xaos.util.log.Log
import java.util.logging.Logger

class JULLogger(name: String) : ILogger {
    private val logger: Logger = Logger.getLogger(name)

    override fun log(level: Level, e: Throwable?, message: () -> String) {
        logger.log(RecordConverter.logRecord(Log.caller(), logger.name, level, message, e))
    }

    override fun isEnabled(level: Level): Boolean = logger.isLoggable(LevelConverter.level(level))
}
