package moe.nea.sky.platform.forge

import me.bush.eventbus.bus.EventBus
import moe.nea.sky.platform.core.Platform
import moe.nea.sky.platform.core.PlatformLoader
import moe.nea.sky.platform.core.apis.ChatAPI
import moe.nea.sky.platform.core.di.DependencyInjection
import moe.nea.sky.platform.core.events.TickEvent
import net.minecraft.client.Minecraft
import net.minecraft.launchwrapper.Launch
import net.minecraft.launchwrapper.LaunchClassLoader
import net.minecraft.util.ChatComponentText
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.lang.invoke.MethodHandles
import java.util.concurrent.ConcurrentLinkedQueue

@Mod(modid = "skyneamoe")
class ForgeEntrypoint : Platform {
    val cachedClasses: ((LaunchClassLoader) -> MutableMap<String, Class<*>>) by lazy {
        val field = LaunchClassLoader::class.java.getDeclaredField("cachedClasses")
        field.isAccessible = true
        val handle = MethodHandles.lookup().unreflectGetter(field)
        return@lazy { handle.invoke(it) as MutableMap<String, Class<*>> }
    }

    override fun unloadClass(name: String) {
        cachedClasses(Launch.classLoader).remove(name)
    }

    init {
        PlatformLoader.start(this)
    }

    @EventHandler
    fun onInit(event: FMLInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun tickEvent(event: ClientTickEvent) {
        if (event.phase == net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END)
            eventBus.post(TickEvent(Minecraft.getMinecraft().thePlayer != null))
        val player = Minecraft.getMinecraft().thePlayer ?: return
        while (true) {
            val msg = messageQueue.poll() ?: break
            player.addChatMessage(ChatComponentText(msg))
        }
    }


    lateinit var eventBus: EventBus
    val messageQueue = ConcurrentLinkedQueue<String>()

    override fun injectPlatform(di: DependencyInjection) {
        di.registerPlatform<ChatAPI> {
            object : ChatAPI {
                override fun sendToPlayer(text: String) {
                    messageQueue.add(text)
                }
            }
        }
    }

    override fun driveBus(eventBus: EventBus) {
        this.eventBus = eventBus
    }

    override fun invokeLater(function: () -> Unit) {
        Minecraft.getMinecraft().addScheduledTask { function() }
    }

    override fun prepareUnload() {

    }

    override fun acquiesceUnload() {

    }

}