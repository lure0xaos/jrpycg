package com.github.lure0xaos.util.log.fmt

import com.github.lure0xaos.util.log.Log
import com.github.lure0xaos.util.log.LoggerRecord

object BasicFormatter : IFormatter {

    override fun format(record: LoggerRecord): String =
        buildString {
            append(Log.dateTime(record.dateTime))
            append(" [").append(record.level.text.padStart(5)).append("] ")
            if (record.className.isNotBlank() && record.methodName.isNotBlank())
                append(" ").append(record.className + "." + record.methodName)
            if (record.message.isNotBlank()) append(": ").append(record.message)
            if (record.error != null) append("\n").append(record.error.stackTraceToString())
            append("\n")
        }
}
