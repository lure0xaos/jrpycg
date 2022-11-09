package com.github.lure0xaos.jrpycg.model

import javax.swing.Icon

enum class VarType(val keyword: String, val text: String, val icon: Icon? = null, val allowed: String? = null) {
    STR("str", "String", null, null),
    INT("int", "Integer", null, "-0123456789"),
    FLOAT("float", "Float", null, "-0123456789.");

    companion object {
        fun find(keyword: String): VarType =
            values().first { it.keyword == keyword }
    }


}
