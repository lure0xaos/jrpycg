package com.github.lure0xaos.jrpycg

import com.github.lure0xaos.jrpycg.ui.RPyCGFrame
import com.github.lure0xaos.util.log.LoggerFactory
import com.github.lure0xaos.util.pref.file.FilePreferencesFactory
import com.github.lure0xaos.util.ui.main.Main
import com.github.lure0xaos.util.ui.preloader.ImagePreloader

object RPyCG {

    @JvmStatic
    fun main(args: Array<String>) {
        LoggerFactory
        FilePreferencesFactory()
        Main.run(ImagePreloader(), RPyCGFrame::class, args)
    }
}
