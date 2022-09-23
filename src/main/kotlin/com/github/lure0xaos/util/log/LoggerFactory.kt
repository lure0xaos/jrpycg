package com.github.lure0xaos.util.log

import com.github.lure0xaos.util.log.fmt.AnsiFormatter
import com.github.lure0xaos.util.log.fmt.IFormatter
import com.github.lure0xaos.util.log.jul.JULLoggerFactory

object LoggerFactory : ILoggerFactory {
    private val factory: ILoggerFactory by lazy { JULLoggerFactory() }
    internal val formatter: IFormatter = AnsiFormatter
    override fun getLogger(name: String): ILogger = factory.getLogger(name)
}
