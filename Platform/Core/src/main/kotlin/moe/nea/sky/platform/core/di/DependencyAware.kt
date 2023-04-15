package moe.nea.sky.platform.core.di

import kotlin.reflect.KClass

interface DependencyAware {
    val di: DependencyInjection
}

inline fun <reified T : Any> DependencyAware.lazyDI(): Lazy<T> {
    return lazyDI(T::class)
}

fun <T : Any> DependencyAware.lazyDI(clazz: KClass<T>): Lazy<T> {
    return lazy {
        di.inject(clazz)
    }
}