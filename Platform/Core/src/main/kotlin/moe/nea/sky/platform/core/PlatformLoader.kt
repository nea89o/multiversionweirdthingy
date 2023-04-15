package moe.nea.sky.platform.core

import me.bush.eventbus.bus.EventBus
import me.bush.eventbus.handler.handlers.ASMHandler
import me.bush.eventbus.handler.handlers.ReflectHandler
import moe.nea.sky.platform.core.di.DependencyInjection
import java.nio.file.Path
import java.nio.file.Paths

class PlatformLoader private constructor(val platform: Platform) {
    companion object {
        fun start(platform: Platform) {
            val p = PlatformLoader(platform)
            p.restartNow()
        }
    }

    var eventBus = EventBus(ReflectHandler::class.java)
        private set
    var info = mutableMapOf<String, ModuleInfo>()
        private set
    var modules = mutableMapOf<String, ConstructedModule>()
        private set
    var classLoader = ModuleClassLoader(info, platform.javaClass.classLoader)
        private set
    var di = DependencyInjection()
        private set

    /**
     * Schedule a restart. Makes sure all coroutines are closed
     */
    fun scheduleRestart() {
        platform.prepareUnload()
        platform.invokeLater {
            restartNow()
        }
    }

    /**
     * Perform a restart. Should not be called from within a module.
     */
    fun restartNow() {
        unloadModules()
        platform.injectPlatform(di)
        platform.driveBus(eventBus)
        discoverModules()
        loadModules()
    }

    private fun discoverModules() {
        info["core"] = ModuleInfo(
            // TODO real module discovery
            "Core",
            Paths.get("/home/nea/src/skyneamoe/Modules/Core/build/classes/kotlin/main"),
            Paths.get("/home/nea/src/skyneamoe/Modules/Core/build/resources/main"),
            "core",
            listOf()
        )
    }

    private fun loadModules() {
        info.values.forEach {
            // TODO name from module info
            val clazz = Class.forName("moe.nea.sky.core.Core", true, classLoader)
            val instance = clazz.getDeclaredConstructor().newInstance() as Module
            instance.construct(it)
            modules[it.identifier] = ConstructedModule(it, instance)
            instance.setupInjection(di)
        }
        modules.values.forEach {
            it.instance.subscribe(eventBus)
        }
    }


    private fun unloadModules() {
        val oldInfo = info
        info = mutableMapOf()
        modules = mutableMapOf()
        eventBus = EventBus(ReflectHandler::class.java)
        classLoader.loadedClasses.forEach {
            platform.unloadClass(it)
        }
        classLoader = ModuleClassLoader(info, platform.javaClass.classLoader)
        oldInfo.forEach { it.value.destroy() }
        System.gc()
    }

}