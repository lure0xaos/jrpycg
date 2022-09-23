package com.github.lure0xaos.util.audio

import java.io.Closeable

interface AudioClip : Closeable {
    fun play()
    fun loop()
    fun stop()
}
