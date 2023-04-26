package moe.nea.sky.platform.forge

import me.bush.eventbus.bus.EventBus
import moe.nea.sky.platform.core.Platform
import moe.nea.sky.platform.core.PlatformLoader
import moe.nea.sky.platform.core.apis.SchedulingAPI
import moe.nea.sky.platform.core.apis.chat.ChatAPI
import moe.nea.sky.platform.core.apis.chat.Text
import moe.nea.sky.platform.core.apis.chat.TextColor
import moe.nea.sky.platform.core.apis.chat.TextStyle
import moe.nea.sky.platform.core.di.DependencyInjection
import moe.nea.sky.platform.core.events.TickEvent
import net.minecraft.client.Minecraft
import net.minecraft.launchwrapper.Launch
import net.minecraft.launchwrapper.LaunchClassLoader
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.ClientCommandHandler
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
        while (true) {
            val task = scheduledTasks.poll() ?: break
            task.invoke()
        }
        if (event.phase == net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END)
            eventBus.post(TickEvent(Minecraft.getMinecraft().thePlayer != null))
        val player = Minecraft.getMinecraft().thePlayer ?: return
        while (true) {
            val msg = messageQueue.poll() ?: break
            fun transformToPlatform(text: Text, parentStyle: TextStyle): ChatComponentText {
                val raw = when (text) {
                    is Text.Literal -> ChatComponentText(text.text)
                }
                val style = TextStyle(
                    text.style?.color ?: parentStyle.color ?: TextColor.Simple.WHITE,
                    text.style?.bold ?: parentStyle.bold ?: false,
                    text.style?.italic ?: parentStyle.italic ?: false,
                    text.style?.obfuscated ?: parentStyle.obfuscated ?: false,
                    text.style?.strikethrough ?: parentStyle.strikethrough ?: false,
                    text.style?.underlined ?: parentStyle.underlined ?: false,
                )
                raw.setChatStyle(
                    ChatStyle()
                        .setBold(style.bold)
                        .setItalic(style.italic)
                        .setObfuscated(style.obfuscated)
                        .setStrikethrough(style.strikethrough)
                        .setUnderlined(style.underlined)
                        .setColor(EnumChatFormatting.valueOf(style.color!!.toSimpleColor().name))
                )
                text.children.map { transformToPlatform(it, style) }.forEach {
                    raw.appendSibling(it)
                }
                return raw
            }
            player.addChatMessage(transformToPlatform(msg, TextStyle()))
        }
    }


    lateinit var eventBus: EventBus
    val messageQueue = ConcurrentLinkedQueue<Text>()
    val scheduledTasks = ConcurrentLinkedQueue<() -> Unit>()

    override fun injectPlatform(di: DependencyInjection) {
        di.registerPlatform<ChatAPI> {
            object : ChatAPI {
                override fun sendToPlayer(text: Text) {
                    messageQueue.add(text)
                }

                override fun registerCommand(label: String, handler: (List<String>) -> Unit) {
                    ClientCommandHandler.instance.registerCommand(ModuleCommand(label, handler))
                }
            }
        }
        di.registerPlatform<SchedulingAPI> {
            object : SchedulingAPI {
                override fun callSoon(block: () -> Unit) {
                    scheduledTasks.add(block)
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
        ClientCommandHandler.instance.commands.entries.removeAll { it.value is ModuleCommand }
        scheduledTasks.clear()
    }

    override fun acquiesceUnload() {

    }

}