package moe.nea.sky.platform.core.di

import kotlin.reflect.KClass

class DependencyInjectionException(
    val label: String,
    val kClass: KClass<*>
) : Exception("Could not inject $kClass: $label") {

}
