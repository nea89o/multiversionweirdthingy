package moe.nea.sky.core

import me.bush.eventbus.annotation.EventListener
import me.bush.eventbus.bus.EventBus
import moe.nea.sky.platform.core.Module
import moe.nea.sky.platform.core.ModuleInfo
import moe.nea.sky.platform.core.PlatformLoader
import moe.nea.sky.platform.core.apis.chat.ChatAPI
import moe.nea.sky.platform.core.apis.chat.Text
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
                chatApi.sendToPlayer(Text.Literal("Hello, World"))
            }
            tick++
        }
    }

    override fun subscribe(eventBus: EventBus) {
        chatApi.registerCommand("reload") {
            chatApi.sendToPlayer(Text.Literal("Reloading"))
            platform.scheduleRestart()
        }
        println("Subscribing to EventBus")
        eventBus.subscribe(this)
    }
}