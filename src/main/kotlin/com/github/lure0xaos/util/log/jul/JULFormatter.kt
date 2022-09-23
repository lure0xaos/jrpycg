package com.github.lure0xaos.util.log.jul

import com.github.lure0xaos.util.log.LoggerFactory
import java.util.logging.Formatter
import java.util.logging.LogRecord

class JULFormatter : Formatter() {
    override fun format(record: LogRecord): String =
        LoggerFactory.formatter.format(RecordConverter.record(record))


}
