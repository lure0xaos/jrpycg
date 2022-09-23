package com.github.lure0xaos.jrpycg.res.flags

import com.github.lure0xaos.util.findResource
import com.github.lure0xaos.util.getResource
import com.github.lure0xaos.util.toIcon
import javax.swing.Icon

object Flag {

    fun findFlag(name: String): Icon? =
        Flag::class.findResource("$name.png")?.toIcon()

    fun getFlag(name: String): Icon =
        Flag::class.getResource("$name.png").toIcon()
}
