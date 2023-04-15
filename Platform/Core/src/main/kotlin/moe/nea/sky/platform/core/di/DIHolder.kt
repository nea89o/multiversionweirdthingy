package moe.nea.sky.platform.core.di

open class DIHolder : AutoInject, DependencyAware {
    override fun setupInjection(di: DependencyInjection) {
        this.di = di
    }

    final override lateinit var di: DependencyInjection
}