package com.github.lure0xaos.util


class Parameters(args: Array<String>) {
    private val named: Map<String, String> =
        args.associate { arg: String ->
            arg.substringBefore(EQ).trimStart(DASH).trim() to
                    arg.substringAfter(EQ).trim(SPACE, QUOTE, DASH).ifBlank { arg }
        }

    operator fun contains(key: String): Boolean =
        key in named

    operator fun get(key: String): String? =
        named[key]

    operator fun get(key: String, def: String): String =
        named[key] ?: def

    companion object {
        private const val DASH = '-'
        private const val EQ = '='
        private const val SPACE = ' '
        private const val QUOTE = '\"'
    }

}
