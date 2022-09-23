package com.github.lure0xaos.util.audio

import java.io.InputStream
import java.net.URL
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

internal class AudioClipImpl : AudioClip {

    constructor(location: URL) {
        clip = AudioSystem.getClip().apply {
            AudioSystem.getAudioInputStream(location).use { open(it) }
        }
    }

    constructor(location: InputStream) {
        clip = AudioSystem.getClip().apply {
            AudioSystem.getAudioInputStream(location.buffered()).use { open(it) }
        }
    }

    private val clip: Clip

    override fun play() {
        if (clip.isActive) clip.stop()
        if (clip.isOpen) {
            clip.framePosition = 0
            clip.start()
        }
    }

    override fun loop() {
        if (clip.isActive) clip.stop()
        if (clip.isOpen) clip.loop(Clip.LOOP_CONTINUOUSLY)
    }

    override fun stop() {
        if (clip.isActive) clip.stop()
    }

    override fun close() {
        if (clip.isActive) clip.stop()
        if (clip.isOpen) clip.close()
    }
}
