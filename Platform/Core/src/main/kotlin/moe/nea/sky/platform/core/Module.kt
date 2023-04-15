package moe.nea.sky.platform.core

import me.bush.eventbus.bus.EventBus
import moe.nea.sky.platform.core.di.DIHolder

abstract class Module : DIHolder() {
    /**
     * Construction event
     */
    abstract fun construct(moduleInfo: ModuleInfo)

    /**
     * The *real* entrypoint for the module. At this point DI is set up.
     */
    abstract fun subscribe(eventBus: EventBus)
}