package moe.nea.sky.platform.core

import me.bush.eventbus.bus.EventBus
import moe.nea.sky.platform.core.di.DependencyInjection

/**
 * Eternal platform instance, with functions useful outside of module loading.
 */
interface Platform {
    /**
     * Remove references to these classes from the platform class loader.
     */
    fun unloadClass(name: String)

    /**
     * Inject platform instances of core utility modules.
     */
    fun injectPlatform(di: DependencyInjection)

    /**
     * Set the event bus to be driven by the platform. This should cause all old event bus instances to be nulled out.
     */
    fun driveBus(eventBus: EventBus)

    /**
     * Special invoke later that does not use the DI.
     */
    fun invokeLater(function: () -> Unit)

    /**
     * Schedule to unload all references to modules in the platform, such as scheduled executables.
     */
    fun prepareUnload()

    /**
     * Await all references to modules in the platform, such as scheduled executables, to be unloaded.
     */
    fun acquiesceUnload()
}
