package com.github.lure0xaos.util.ui.preloader

import com.github.lure0xaos.util.findResource
import com.github.lure0xaos.util.toIcon
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.Icon
import javax.swing.JLabel


class ImagePreloader(private val image: Icon? = ImagePreloader::class.findResource(LOADING_GIF)?.toIcon()) : Preloader({
    layout = BorderLayout()
    add(JLabel(image).apply {
        background = Color(0, 0, 0, 0)
    })
}) {
    companion object {
        private const val LOADING_GIF = "loading.gif"
    }
}
