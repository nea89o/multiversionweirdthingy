package moe.nea.sky.platform.core.events

import me.bush.eventbus.event.Event

open class BaseEvent : Event() {
    open override fun isCancellable(): Boolean = false
}