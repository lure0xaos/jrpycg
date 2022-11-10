package com.github.lure0xaos.jrpycg.services

import com.github.lure0xaos.jrpycg.model.Settings
import com.github.lure0xaos.util.USER_HOME
import com.github.lure0xaos.util.ui.main.JExtFrame
import java.awt.Desktop
import java.util.prefs.Preferences
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.io.path.writeLines
import kotlin.test.Test
import kotlin.test.assertTrue

@Suppress("SpellCheckingInspection")
internal class CodeGeneratorTest {
    @Test
    internal fun testPrimary() {
        val model = ScriptConverter.fromScript((USER_HOME / "CLIT.rpycg").readLines())
        val code =
            CodeGenerator.generate(model, Settings.fromDefaults(Preferences.userNodeForPackage(JExtFrame::class.java)))
        (Path("C:/") / "Games" / "ChampionsofLibertyInstituteofTraining-0.721-pc" / "game" / "RPyCGCheat.rpy").also {
            it.writeLines(code)
        }.also {
            Desktop.getDesktop().open(it.toFile())
        }.also {
            assertTrue(it.exists())
        }
    }
}
