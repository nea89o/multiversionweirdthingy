package moe.nea.sky.platform.core.apis

interface SchedulingAPI {
    fun callSoon(block: () -> Unit)
}