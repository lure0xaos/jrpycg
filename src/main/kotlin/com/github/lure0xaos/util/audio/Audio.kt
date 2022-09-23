package com.github.lure0xaos.util.audio

import java.io.InputStream
import java.net.URL

object Audio {
    fun URL.newAudioClip(): AudioClip = AudioClipImpl(this)
    fun InputStream.newAudioClip(): AudioClip = AudioClipImpl(this)
}
