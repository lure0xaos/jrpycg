package com.github.lure0xaos.jrpycg.res.icons

import com.github.lure0xaos.util.getResource
import com.github.lure0xaos.util.toIcon
import javax.swing.Icon

enum class ResIcon(value: String) {
    COMPUTER("computer"),
    DELETE("clear"),
    EMPTY("empty"), FILE("text-x-generic"),
    FOLDER("folder"),
    FOLDER_OPEN("folder-open"),
    GAME_FOLDER("game-folder"),
    GAME_FOLDER_OPEN("game-folder-open"),
    MENU("menu"),
    TEMPLATE("template"),
    VARIABLE("var"),

    REQUIRED("required"),

    BUILDER("builder"),
    CREATOR("creator"),
    SETTINGS("settings"),
    ABOUT("about"),

    SAVE("save"),
    GENERATE("generate"),
    RESET("reset"),
    ;

    val icon: Icon = ResIcon::class.getResource("$value.png").toIcon()
}
