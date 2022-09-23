package com.github.lure0xaos.jrpycg.model

import javax.swing.Icon

enum class VarType(val keyword: String, val text: String, val icon: Icon? = null) {
    STR("str", "String", null),
    INT("int", "Integer", null),
    FLOAT("float", "Float", null);

    companion object {
        fun find(keyword: String): VarType =
            values().first { it.keyword == keyword }
    }


}
