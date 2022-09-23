package com.github.lure0xaos.util.log.fmt

import com.github.lure0xaos.util.log.LoggerRecord

fun interface IFormatter {
    fun format(record: LoggerRecord): String
}
