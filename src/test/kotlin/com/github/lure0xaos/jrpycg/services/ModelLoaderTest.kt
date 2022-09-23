package com.github.lure0xaos.jrpycg.services

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class ModelLoaderTest {

    @Test
    fun save() {
    }

    @Test
    fun load() {
        val result = ScriptConverter.fromScript(listOf("name=val(str);lbl"))
        assertNotNull(result)
    }

}
