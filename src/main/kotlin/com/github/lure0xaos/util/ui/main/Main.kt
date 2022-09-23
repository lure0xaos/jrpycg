package com.github.lure0xaos.util.ui.main

import com.github.lure0xaos.util.USER_DIR
import com.github.lure0xaos.util.privileged
import com.github.lure0xaos.util.ui.preloader.Preloader
import com.github.lure0xaos.util.ui.swing
import java.net.URLClassLoader
import kotlin.reflect.KClass

object Main {

    fun run(preloader: Preloader, clazz: KClass<out JExtFrame>, args: Array<String>) {
        preloader.show()
        preloader.notifyProgress(1)
        runMe(preloader, clazz.qualifiedName!!, args)
    }

    private fun runMe(preloader: Preloader, className: String, args: Array<String>) {
        loadClass<JExtFrame>(className)
            .getConstructor(Preloader::class.java, Array<String>::class.java)
            .newInstance(preloader, args).doShow {
                if (it == Return.RESTART) swing {
                    runMe(preloader, className, args)
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <A : Any?> loadClass(className: String): Class<A> =
        privileged<Class<A>> {
            URLClassLoader(arrayOf(USER_DIR.toUri().toURL()), ClassLoader.getSystemClassLoader())
                .loadClass(className) as Class<A>
        }

}
