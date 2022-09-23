package com.github.lure0xaos.util.log

import java.io.Closeable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object Log {
    fun error(message: () -> String): Unit = log(Level.ERROR, null, message)
    fun error(e: Throwable, message: () -> String): Unit = log(Level.ERROR, e, message)
    fun warn(message: () -> String): Unit = log(Level.WARNING, null, message)
    fun warn(e: Throwable, message: () -> String): Unit = log(Level.WARNING, e, message)
    fun info(message: () -> String): Unit = log(Level.INFO, null, message)
    fun info(e: Throwable, message: () -> String): Unit = log(Level.INFO, e, message)
    fun debug(message: () -> String): Unit = log(Level.DEBUG, null, message)
    fun debug(e: Throwable, message: () -> String): Unit = log(Level.DEBUG, e, message)
    fun trace(message: () -> String): Unit = log(Level.TRACE, null, message)
    fun trace(e: Throwable, message: () -> String): Unit = log(Level.TRACE, e, message)
    fun log(level: Level, e: Throwable? = null, message: () -> String): Unit =
        LoggerFactory.getLogger(caller().className).log(level, e, message)

    internal fun caller(): StackTraceElement =
        Thread.currentThread().stackTrace.first { element: StackTraceElement ->
            element.className.let { className: String ->
                !className.contains('$') && packages.none { (className.startsWith(it)) }
            }
        }

    private val packages: Array<String> = arrayOf(
        "java.",
        "javax.",
        "kotlin.",
        "kotlinx.",
        "sun.",
        "com.sun.",
        Log::class.qualifiedName!!.substringBeforeLast('.')
    )

    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    fun dateTime(dateTime: Instant): String =
        dateTime.atZone(ZoneId.systemDefault()).format(dateTimeFormatter)


    @Suppress("NOTHING_TO_INLINE")
    inline fun <R> (() -> R).logging(message: String): R {
        val tracing = traceEnter(message)
        try {
            val r: R = this()
            traceExit(message, tracing)
            return r
        } catch (e: Throwable) {
            traceError(message, e, tracing)
            throw e
        }
    }

    inline fun <T, R> T.runLogging(message: String, block: T.() -> R): R {
        val tracing = traceEnter(message)
        try {
            val r: R = block()
            traceExit(message, tracing)
            return r
        } catch (e: Throwable) {
            traceError(message, e, tracing)
            throw e
        }
    }

    class Tracing(private val message: String, private val start: Long) : Closeable {
        private var end: Long = 0
        private var elapsed: Long = 0

        override fun close() {
            end = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            elapsed = end.minus(start)
        }

        override fun toString(): String {
            return "${elapsed}ms"
        }
    }

    fun traceEnter(message: String): Tracing {
        trace { "ENTER $message" }
        return Tracing(message, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
    }

    fun traceExit(message: String, tracing: Tracing) {
        tracing.close()
        trace { "EXIT $message ($tracing)" }
    }

    fun traceError(message: String, e: Throwable, tracing: Tracing) {
        tracing.close()
        trace(e) { "ERROR $message($tracing)" }
    }

}
