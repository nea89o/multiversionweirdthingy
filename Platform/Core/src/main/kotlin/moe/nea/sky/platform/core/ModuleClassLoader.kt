package moe.nea.sky.platform.core

import java.net.URL
import java.util.*
import kotlin.io.path.exists

class ModuleClassLoader(val modules: Map<String, ModuleInfo>, parent: ClassLoader) : ClassLoader(parent) {
    val loadedClasses = mutableListOf<String>()
    override fun findResource(name: String): URL {
        return findResources(name).iterator().next()
    }

    override fun findResources(name: String): Enumeration<URL> {
        return Collections.enumeration(modules.filter { it.value.location.resolve(name).exists() }
            .map { URL("skyneamoe", it.key, 0, name, ModuleStreamHandler(modules)) })
    }

    override fun findClass(name: String): Class<*> {
        loadedClasses.add(name)
        val path = name.replace(".", "/") + ".class"
        val bytes = modules.mapNotNull { it.value.getInputStream(path).use { it.readBytes() } }.firstOrNull()
            ?: throw ClassNotFoundException(name)
        return defineClass(name, bytes, 0, bytes.size)
    }
}