package com.github.lure0xaos.jrpycg.services

import com.github.lure0xaos.jrpycg.model.ModelItem
import com.github.lure0xaos.jrpycg.model.VarType

object TemplateGenerator {
    fun createTemplate(): ModelItem =
        ModelItem.createRoot {
            createVariable("item1", "item1", "", VarType.STR)
            createMenu("menu1", "menu1") {
                createVariable("item11", "item11", "", VarType.STR)
                createVariable("item12", "item12", "", VarType.STR)
            }
            createMenu("menu2", "menu2") {
                createVariable("item21", "item21", "", VarType.STR)
                createVariable("item22", "item22", "", VarType.STR)
            }
            createVariable("item2", "item2", "", VarType.STR)
        }
}
