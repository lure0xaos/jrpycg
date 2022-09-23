package com.github.lure0xaos.jrpycg.model

import javax.swing.Icon

enum class VarType(val keyword: String, val text: String, val icon: Icon? = null, val allowed: String? = null) {
    STR("str", "String"),
    INT("int", "Integer", allowed = "-0123456789"),
    FLOAT("float", "Float", allowed = "-0123456789.");

    companion object {
        fun find(keyword: String): VarType =
            entries.first { it.keyword == keyword }
    }


}
