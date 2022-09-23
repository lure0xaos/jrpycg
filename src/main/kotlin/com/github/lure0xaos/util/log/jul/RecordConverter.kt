package com.github.lure0xaos.util.log.jul

import com.github.lure0xaos.util.log.Level
import com.github.lure0xaos.util.log.LoggerRecord
import java.util.logging.LogRecord

object RecordConverter {

    fun logRecord(
        caller: StackTraceElement?,
        logger: String,
        level: Level,
        message: () -> String,
        e: Throwable?
    ): LogRecord =
        LogRecord(LevelConverter.level(level), message()).apply {
            loggerName = logger
            thrown = e
            sourceClassName = caller?.className
            sourceMethodName = caller?.methodName
        }

    fun record(record: LogRecord): LoggerRecord =
        LoggerRecord(
            LevelConverter.level(record.level),
            record.message ?: "",
            record.loggerName ?: "",
            record.instant,
            record.thrown,
            record.sourceClassName ?: "",
            record.sourceMethodName ?: ""
        )
}
