package com.github.lure0xaos.jrpycg.model

class ModelItem private constructor(
    private val menuType: ModelType,
    val name: String,
    val label: String,
    val value: String,
    val type: VarType,
    val parent: ModelItem? = null,
    val children: MutableList<ModelItem> = mutableListOf()
) {
    fun isMenu(): Boolean = menuType == ModelType.MENU
    fun isVariable(): Boolean = menuType == ModelType.VAR
    fun isRoot(): Boolean = menuType == ModelType.ROOT

    fun createMenu(name: String, label: String): ModelItem {
        require(isMenu() || isRoot())
        return ModelItem(ModelType.MENU, name, label, "", VarType.STR, this).also {
            children += it
        }
    }

    fun createVariable(name: String, label: String, value: String, type: VarType): ModelItem {
        require(isMenu() || isRoot())
        return ModelItem(ModelType.VAR, name, label, value, type, this).also {
            children += it
        }
    }

    override fun equals(other: Any?): Boolean =
        if (this === other) true else if (other !is ModelItem) false else name == other.name

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String =
        "CheatItem(menuType=$menuType, name='$name', label='$label', value='$value', type=$type)"


    companion object {
        fun createRoot(): ModelItem =
            ModelItem(ModelType.ROOT, "", "", "", VarType.STR)

    }

}
