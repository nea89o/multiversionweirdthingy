package moe.nea.sky.core

import me.bush.eventbus.annotation.EventListener
import me.bush.eventbus.bus.EventBus
import moe.nea.sky.platform.core.Module
import moe.nea.sky.platform.core.ModuleInfo
import moe.nea.sky.platform.core.PlatformLoader
import moe.nea.sky.platform.core.apis.ChatAPI
import moe.nea.sky.platform.core.di.lazyDI
import moe.nea.sky.platform.core.events.TickEvent

class Core : Module() {
    init {
        println("Static initialization of ${Core::class}")
    }

    val chatApi by lazyDI<ChatAPI>()
    val platform by lazyDI<PlatformLoader>()

    override fun construct(moduleInfo: ModuleInfo) {
        println("Constructing Core with $moduleInfo")
    }
    var tick = 0

    @EventListener
    fun onLoad(event: TickEvent) {
        if (event.isInWorld) {
            if (tick == 0) {
                chatApi.sendToPlayer("Hello, World")
            }
            if (tick++ == 1000) {
                chatApi.sendToPlayer("Reloading")
                platform.scheduleRestart()
            }
        }
    }

    override fun subscribe(eventBus: EventBus) {
        println("Subscribing to EventBus")
        eventBus.subscribe(this)
    }
}