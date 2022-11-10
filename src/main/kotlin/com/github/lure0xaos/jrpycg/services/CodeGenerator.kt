package com.github.lure0xaos.jrpycg.services

import com.github.lure0xaos.jrpycg.model.ModelItem
import com.github.lure0xaos.jrpycg.model.Settings
import com.github.lure0xaos.jrpycg.model.VarType
import com.github.lure0xaos.jrpycg.res.Res
import com.github.lure0xaos.util.StringListBuilder.Companion.buildStringList
import com.github.lure0xaos.util.findResourceBundleLocales
import com.github.lure0xaos.util.format
import com.github.lure0xaos.util.get
import com.github.lure0xaos.util.getResourceBundle
import com.github.lure0xaos.util.resolveName
import java.util.Locale
import java.util.ResourceBundle

object CodeGenerator {
    val locales: Array<Locale> =
        CodeGenerator::class.findResourceBundleLocales(CodeGenerator::class.resolveName(), Locale.ENGLISH)
            .toTypedArray()

    fun generate(menu: ModelItem, settings: Settings): List<String> =
        Res::class.getResourceBundle(CodeGenerator::class, settings.localeMenu).let { resources: ResourceBundle ->
            listOf("init 999 python:").also { require(menu.isRoot()) } +
                    (if (settings.enableConsole) enableConsole(settings).indent(1) else listOf()) +
                    (if (settings.enableDeveloper) enableDeveloper(settings).indent(1) else listOf()) +
                    (if (settings.enableRollback) enableRollback().indent(1) else listOf()) +
                    (if (settings.enableCheat) enableCheat(settings).indent(1) else listOf()) +
                    (if (settings.enableWrite) enableWrite(settings).indent(1) else listOf()) +
                    (if (settings.enableWrite) enableWrite2(resources) else listOf()) +
                    (if (settings.enableCheat) createCheatMenu(menu, resources) else listOf())
        }

    private fun enableConsole(settings: Settings): List<String> =
        listOf(
            "# Enable console",
            "config.console = True",
            "persistent._console_short = False",
            "# Enable fast console",
            "config.keymap[\"console\"] = [\"${settings.keyConsole.toBinding()}\"]"
        )

    private fun enableDeveloper(settings: Settings): List<String> =
        listOf(
            "# Enable developer mode",
            "config.developer = True",
            "# Enable developer mode",
            "config.keymap[\"developer\"] = [\"${settings.keyDeveloper.toBinding()}\"]"
        )

    private fun enableCheat(settings: Settings): List<String> =
        listOf(
            "# Define function to open the menu",
            "def rpycg_enable_cheat_menu():",
            "    renpy.call_in_new_context(\"show_cheat_menu\")",
            "def rpycg_exists(obj, attr):",
            "    try:",
            "        left, right = attr.split('.', 1)",
            "    except:",
            "        return hasattr(obj, attr)",
            "    if hasattr(obj, left):",
            "        return rpycg_exists(getattr(obj, left), right)",
            "    else:",
            "        return False",
            "config.keymap[\"cheat_menu_bind\"] = [\"${settings.keyCheat.toBinding()}\"]",
            "config.underlay.append(renpy.Keymap(cheat_menu_bind=rpycg_enable_cheat_menu))"
        )

    private fun enableRollback(): List<String> =
        listOf(
            "# Enable rollback",
            "config.rollback_enabled = True",
            "renpy.config.hard_rollback_limit = 256",
            "renpy.config.rollback_length = 256",
            "def rpycg_noblock( *args, **kwargs ):",
            "    return",
            "renpy.block_rollback = rpycg_noblock",
            "try:",
            "    config.keymap['rollback'] = ['K_PAGEUP', 'repeat_K_PAGEUP', 'K_AC_BACK', 'mousedown_4']",
            "except:",
            "    pass"
        )

    private fun enableWrite(settings: Settings): List<String> =
        listOf(
            "# Define function to write variables to file",
            "def rpycg_write_variables():",
            "    renpy.call_in_new_context('write_variables_to_file')",
            "executions = 0",
            "max_executions = 100000",
            "def rpycg_dump_obj(obj, obj_name = '', level = 0):",
            "    global executions",
            "    global max_executions",
            "    obj_type = type(obj).__name__",
            "    try:",
            "        if obj_name in ['image_gallery', 'random']:",
            "            return '\\n ERROR BLACKLISTED  (%s) %s %s\\n' % (obj_type, obj_name, level)",
            "        if level > 10:",
            "            return '\\n ERROR RECURSION  (%s) %s %s\\n' % (obj_type, obj_name, level)",
            "        executions += 1",
            "        if executions > max_executions:",
            "            return '\\n ERROR TOO MANY x (%s) %s %s\\n' % (obj_type, obj_name, level)",
            "        text = ''",
            "        if obj is None:",
            "            text = '%s -> %s' % (obj_name, 'None')",
            "        elif isinstance(obj, str) or isinstance(obj, unicode) or isinstance(obj, bool) or isinstance(obj, int) or isinstance(obj, float):",
            "            text = '%s -> %s' % (obj_name, str(repr(obj)))",
            "        elif isinstance(obj, dict):",
            "            for key, value in obj.items():",
            "                text += rpycg_dump_obj(value, '(%s)%s.%s' % (obj_type, obj_name, key), level + 1)",
            "                executions += 1",
            "                if executions > max_executions:",
            "                    return '\\n ERROR TOO MANY d %s.%s - %s - %s: %s' % (obj_name, key, level, len(obj.items()), str(obj.items()))",
            "        elif isinstance(obj, tuple) or isinstance(obj, list) or isinstance(obj, set) or isinstance(obj, frozenset):",
            "            for key, value in enumerate(obj):",
            "                text += rpycg_dump_obj(value, '(%s)%s.%s' % (obj_type, obj_name, key), level + 1)",
            "                executions += 1",
            "                if executions > max_executions:",
            "                    return '\\n ERROR TOO MANY s %s.%s - %s - %s: %s' % (obj_name, key, level, len(obj), '')",
            "        elif hasattr(obj, '__dict__'):",
            "            for key, value in obj.__dict__.items():",
            "                if obj is not value and not key.startswith('__') and not type(value).__name__.startswith('store.'):",
            "                    text += rpycg_dump_obj(value, '%s.%s' % (obj_name, key), level + 1)",
            "                executions += 1",
            "                if executions > max_executions:",
            "                    return '\\n ERROR TOO MANY o %s.%s - %s - %s: %s' % (obj_name, key, level, len(obj.__dict__.items()), str(obj.__dict__.items()))",
            "        elif hasattr(obj, 'items'):",
            "            for key, value in obj.items():",
            "                if obj is not value and not key.startswith('__') and not type(value).__name__.startswith('store.'):",
            "                    text += rpycg_dump_obj(value, '%s.%s' % (obj_name, key), level + 1)",
            "                executions += 1",
            "                if executions > max_executions:",
            "                    return '\\n ERROR TOO MANY o %s.%s - %s - %s: %s' % (obj_name, key, level, len(obj.items()), str(obj.items()))",
            "        else:",
            "            return '\\n ERROR UNKNOWN %s  (%s) %s\\n' % (obj_name, obj_type, obj_name)",
            "    except Exception as error:",
            "        return '\\n ERROR EXCEPTION %s  (%s) %s\\n' % (str(error), obj_type, obj_name)",
            "    return ('\\n  ' * level) + text",
            "# Enable write variables to file",
            "config.keymap[\"write_variables_bind\"] = [\"${settings.keyWrite.toBinding()}\"]",
            "config.underlay.append(renpy.Keymap(write_variables_bind=rpycg_write_variables))",
        )

    private fun enableWrite2(resources: ResourceBundle): List<String> =
        listOf(
            "# Find unique game variables",
            "label write_variables_to_file:",
            "    $ rpycg_f = open(\"${resources[LC_FILE_VARIABLES, MSG_GAME_VARIABLES]}.txt\", \"w+\")",
            "    define rpycg_in_game_defaults = set(\",\".join(globals()).split(\",\"))",
            "    $ rpycg_in_game_diff = \"\\n\".join(sorted(set(set(\",\".join(globals()).split(\",\"))).difference(rpycg_in_game_defaults))).split(\"\\n\")",
            "    define rpycg_new_game_defaults = []",
            "    python:",
            "        for item in rpycg_in_game_diff:",
            "            if not str(item) in [\"rpycg_f\", \"rpycg_exists\", \"rpycg_enable_cheat_menu\", \"rpycg_write_variables\", \"rpycg_new_game_defaults\", \"rpycg_in_game_defaults\", \"rpycg_in_game_diff\", \"_history_list\"]:",
            "                rpycg_new_game_defaults.append(str(item) + \" = \" + str(rpycg_dump_obj(globals().get(item), item)) + \"\\n\")",
            "    $ rpycg_f.write(\"\\n\".join([unicode(i) for i in rpycg_new_game_defaults]))",
            "    $ rpycg_f.close()",
            "    \"${resources[LC_MESSAGE_WRITTEN, MSG_VARIABLES_WRITTEN]}\"",
            "    return"
        )


    private fun createCheatMenu(root: ModelItem, resources: ResourceBundle): List<String> =
        listOf(
            "label show_cheat_menu:",
            "    jump rpycg_cheat_menu",
            "label rpycg_cheat_menu:",
            "    menu:"
        ) + createCheatSubmenu(root, "rpycg_cheat_menu", resources).indent(2) + listOf(
            "        # nevermind",
            "        \"~${resources[LC_NEVERMIND, MSG_NEVERMIND]}~\":",
            "            return"
        )

    private fun createCheatSubmenu(root: ModelItem, parentLabel: String, resources: ResourceBundle): List<String> =
        buildStringList {
            for (item in root.children) {
                val itemName = item.name
                val itemLabel = item.label
                when {
                    item.isVariable() -> {
                        val itemType = item.type
                        val itemValue = item.value
                        +"# variable ${itemName}=${itemType}(${itemValue}) $itemLabel"
                        val itemTypeKeyword = itemType.keyword
                        if (item.value.isNotBlank()) {
                            +"\"$${itemLabel}=${itemValue} \\[[${itemName}]\\]\"${check(itemName)}:"
                            +("    $${itemName} = " +
                                    (if (VarType.STR == itemType) "${itemTypeKeyword}(\"${itemValue}\")"
                                    else "${itemTypeKeyword}(\"${itemValue}\")"))
                        } else {
                            +"\"$itemLabel \\[[${itemName}]\\]\" ${check(itemName)}:"
                            val message = resources[LC_MESSAGE_PROMPT, MSG_MESSAGE_PROMPT].format(
                                mapOf(
                                    "label" to itemLabel,
                                    "value" to "[$itemName]"
                                )
                            )
                            val allow = itemType.allowed?.let { ", allow=\"$it\"" } ?: ""
                            +"    $${itemName} = ${itemTypeKeyword}(renpy.input(\"$message\"$allow).strip() or ${itemName})"
                        }
                        +"    jump $parentLabel"
                    }

                    item.isMenu() -> {
                        val pythonLabel = "rpycg_$itemName"
                        +"# menu $itemLabel"
                        +"\"~${itemLabel}~\":"
                        +"    label ${pythonLabel}:"
                        +"        menu:"
                        +createCheatSubmenu(item, pythonLabel, resources).indent(3)
                        +"            # back"
                        +"            \"~${resources[LC_BACK, MSG_BACK]}~\":"
                        +"                jump $parentLabel"
                    }
                }
            }
        }

    private fun check(itemName: String): String =
        if (itemName.contains('[')) ""
        else if (itemName.contains('.'))
            "if '${itemName.substringBefore('.')}' in globals()" +
                    " and rpycg_exists(globals()['${itemName.substringBefore('.')}'], '${itemName.substringAfter('.')}')"
        else "if '${itemName}' in globals()"

    private fun List<String>.indent(count: Int, string: String = "    "): List<String> =
        this.map { string.repeat(count) + it }

    private const val LC_BACK = "back"
    private const val LC_FILE_VARIABLES = "file-variables"
    private const val LC_MESSAGE_PROMPT = "message-prompt"
    private const val LC_MESSAGE_WRITTEN = "message-written"
    private const val LC_NEVERMIND = "nevermind"
    private const val MSG_BACK = "Back"
    private const val MSG_GAME_VARIABLES = "Game Variables"
    private const val MSG_MESSAGE_PROMPT = "Change {0} from {1} to"
    private const val MSG_NEVERMIND = "Nevermind"
    private const val MSG_VARIABLES_WRITTEN = "Game variables written to file."
}
