package com.github.lure0xaos.util.log.fmt

import com.github.lure0xaos.util.log.Level
import com.github.lure0xaos.util.log.Log
import com.github.lure0xaos.util.log.LoggerRecord

object AnsiFormatter : IFormatter {

    override fun format(record: LoggerRecord): String =
        buildString {
            append(Ansi.WHITE.invoke(Log.dateTime(record.dateTime)))
            append(" [").append(color(record.level).invoke(record.level.text.padStart(5))).append("] ")
            if (record.className.isNotBlank() && record.methodName.isNotBlank())
                append(" ").append(Ansi.WHITE.invoke(record.className + "." + record.methodName))
            if (record.message.isNotBlank()) append(": ").append(Ansi.WHITE.invoke(record.message))
            if (record.error != null) append("\n").append(Ansi.WHITE.invoke(record.error.stackTraceToString()))
            append("\n")
        }


    private fun color(level: Level) =
        when (level) {
            Level.ERROR -> Ansi.BRIGHT_RED
            Level.WARNING -> Ansi.BRIGHT_YELLOW
            Level.INFO -> Ansi.BRIGHT_BLUE
            Level.DEBUG -> Ansi.DARK_WHITE
            Level.TRACE -> Ansi.DARK_WHITE
        }

    @Suppress("unused")
    enum class Ansi(private val code: String) {
        BLACK("\u001b[30m"),
        BACK_BLACK("\u001b[40m"),
        DARK_RED("\u001b[31m"),
        BACK_DARK_RED("\u001b[41m"),
        DARK_GREEN("\u001b[32m"),
        BACK_DARK_GREEN("\u001b[42m"),
        DARK_YELLOW("\u001b[33m"),
        BACK_DARK_YELLOW("\u001b[43m"),
        DARK_BLUE("\u001b[34m"),
        BACK_DARK_BLUE("\u001b[44m"),
        DARK_MAGENTA("\u001b[35m"),
        BACK_DARK_MAGENTA("\u001b[45m"),
        DARK_CYAN("\u001b[36m"),
        BACK_DARK_CYAN("\u001b[46m"),
        DARK_WHITE("\u001b[37m"),
        BACK_DARK_WHITE("\u001b[47m"),
        BRIGHT_BLACK("\u001b[90m"),
        BACK_BRIGHT_BLACK("\u001b[100m"),
        BRIGHT_RED("\u001b[91m"),
        BACK_BRIGHT_RED("\u001b[101m"),
        BRIGHT_GREEN("\u001b[92m"),
        BACK_BRIGHT_GREEN("\u001b[102m"),
        BRIGHT_YELLOW("\u001b[93m"),
        BACK_BRIGHT_YELLOW("\u001b[103m"),
        BRIGHT_BLUE("\u001b[94m"),
        BACK_BRIGHT_BLUE("\u001b[104m"),
        BRIGHT_MAGENTA("\u001b[95m"),
        BACK_BRIGHT_MAGENTA("\u001b[105m"),
        BRIGHT_CYAN("\u001b[96m"),
        BACK_BRIGHT_CYAN("\u001b[106m"),
        WHITE("\u001b[97m"),
        BACK_WHITE("\u001b[107m"),

        RESET("\u001b[0m");

        operator fun invoke(text: String): String = code + text + RESET.code

        companion object {
            operator fun invoke(text: String, vararg ansi: Ansi): String =
                ansi.fold(text) { txt: String, code: Ansi -> code(txt) }
        }

    }
}
