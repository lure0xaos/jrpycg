package com.github.lure0xaos.util

fun List<String>.indent(count: Int, prefix: String = "    "): List<String> =
    this.map { prefix.repeat(count) + it }

@Suppress("unused")
fun indent(count: Int, prefix: String = "    ", body: () -> List<String>): List<String> =
    body().map { prefix.repeat(count) + it }
