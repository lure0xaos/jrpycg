package com.github.lure0xaos.util.log

import java.time.Instant

data class LoggerRecord(
    val level: Level,
    val message: String,
    val loggerName: String = "",
    val dateTime: Instant = Instant.now(),
    val error: Throwable?,
    val className: String = "",
    val methodName: String = ""
)
