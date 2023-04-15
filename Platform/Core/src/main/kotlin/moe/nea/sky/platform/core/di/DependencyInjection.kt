package moe.nea.sky.platform.core.di

import kotlin.reflect.KClass

class DependencyInjection {
    private val platformInitalizers = mutableMapOf<KClass<*>, () -> Any>()
    private val availableObjects = mutableMapOf<KClass<*>, Any?>()
    private val initializers = mutableMapOf<KClass<*>, () -> Any>()
    private val currentlyMakingAvailable = mutableSetOf<KClass<*>>()

    inline fun <reified T : Any> register(noinline init: () -> T) {
        register(T::class, init)
    }

    fun <T : Any> register(kClass: KClass<T>, init: () -> T) {
        initializers[kClass] = init
    }

    inline fun <reified T : Any> registerPlatform(noinline init: () -> T) {
        register(T::class, init)
    }

    fun <T : Any> registerPlatform(kClass: KClass<T>, init: () -> T) {
        platformInitalizers[kClass] = init
        initializers[kClass] = init
    }

    inline fun <reified T : Any> inject() = inject(T::class)
    fun <T : Any> inject(clazz: KClass<T>): T {
        val obj = availableObjects[clazz]
        if (obj == null) {
            return makeAvailable(clazz)
        }
        return obj as T
    }

    inline fun <reified T : Any> lazy() = lazy(T::class)
    fun <T : Any> lazy(clazz: KClass<T>): Lazy<T> {
        return lazy(LazyThreadSafetyMode.NONE) { inject(clazz) }
    }

    private fun <T : Any> makeAvailable(clazz: KClass<T>): T {
        synchronized(this) {
            if (clazz in currentlyMakingAvailable)
                throw DependencyInjectionException(
                    "Recursive initialization: $currentlyMakingAvailable",
                    clazz
                )
            val any = initializers[clazz]
                ?: throw DependencyInjectionException(
                    "No initalizer found",
                    clazz
                )
            var obj = availableObjects[clazz]
            if (obj != null) {
                return obj as T
            }
            currentlyMakingAvailable.add(clazz)
            obj = any() as T
            availableObjects[clazz] = obj
            initializers.remove(clazz)
            currentlyMakingAvailable.remove(clazz)
            if (obj is AutoInject) {
                obj.setupInjection(this)
            }
            return obj
        }
    }
}