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
    val isMenu: Boolean = menuType == ModelType.MENU
    val isVariable: Boolean = menuType == ModelType.VAR
    val isAction: Boolean = menuType == ModelType.ACTION
    val isRoot: Boolean = menuType == ModelType.ROOT

    fun createMenu(name: String, label: String, initializer: ModelItem.() -> Unit = { }): ModelItem {
        require(isMenu || isRoot)
        return ModelItem(ModelType.MENU, name, label, "", VarType.STR, this).also {
            children += it
        }.apply(initializer)
    }

    fun createVariable(name: String, label: String, value: String, type: VarType): ModelItem {
        require(isMenu || isRoot)
        return ModelItem(ModelType.VAR, name, label, value, type, this).also {
            children += it
        }
    }

    fun createAction(name: String, label: String, value: String): ModelItem {
        require(isMenu || isRoot)
        return ModelItem(ModelType.ACTION, name, label, value, VarType.STR, this).also {
            children += it
        }
    }

    override fun equals(other: Any?): Boolean =
        if (this === other) true else if (other !is ModelItem) false else name == other.name

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String =
        "CheatItem(menuType=$menuType, name='$name', label='$label', value='$value', type=$type)"


    companion object {
        fun createRoot(initializer: ModelItem.() -> Unit = { }): ModelItem =
            ModelItem(ModelType.ROOT, "", "", "", VarType.STR).apply(initializer)

    }

}
