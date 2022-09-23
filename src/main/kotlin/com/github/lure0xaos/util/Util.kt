package com.github.lure0xaos.util

fun <E : Any> MutableList<E>.setAll(list: List<E>) {
    clear()
    this += list
}
