package moe.nea.sky.platform.fabric

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
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
import moe.nea.sky.platform.fabric.mixin.AccessorCommandNode
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Style
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import net.minecraft.text.Text as MText

object FabricEntryPoint : ModInitializer, Platform {

    lateinit var eventBus: EventBus

    override fun onInitialize() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            scheduledExecutables.forEach { it.invoke() }
            eventBus.post(TickEvent(MinecraftClient.getInstance().player != null))
            val player = MinecraftClient.getInstance().player ?: return@EndTick
            fun transformToPlatform(text: Text, parentStyle: TextStyle): MText {
                val raw = when (text) {
                    is Text.Literal -> MText.literal(text.text)
                }
                val style = TextStyle(
                    text.style?.color ?: parentStyle.color ?: TextColor.Simple.WHITE,
                    text.style?.bold ?: parentStyle.bold ?: false,
                    text.style?.italic ?: parentStyle.italic ?: false,
                    text.style?.obfuscated ?: parentStyle.obfuscated ?: false,
                    text.style?.strikethrough ?: parentStyle.strikethrough ?: false,
                    text.style?.underlined ?: parentStyle.underlined ?: false,
                )
                raw.setStyle(
                    Style.EMPTY
                        .withBold(style.bold)
                        .withItalic(style.italic)
                        .withObfuscated(style.obfuscated)
                        .withStrikethrough(style.strikethrough)
                        .withUnderline(style.underlined)
                        .withColor(net.minecraft.text.TextColor.fromRgb(style.color!!.toRGB()))
                )
                text.children.map { transformToPlatform(it, style) }.forEach {
                    raw.append(it)
                }
                return raw
            }
            while (true) {
                val message = messageQueue.poll() ?: break
                player.sendMessage(transformToPlatform(message, TextStyle()))
            }
        })
        ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { commandDispatcher, commandRegistryAccess ->
            tryRegisterAllCommands(commandDispatcher)
        })
        PlatformLoader.start(this)
    }

    override fun unloadClass(name: String) {
    }

    val scheduledExecutables = ConcurrentLinkedQueue<() -> Unit>()
    val messageQueue = ConcurrentLinkedQueue<Text>()
    val commandsToUnregister = mutableListOf<String>()
    val dispatchersToUnregisterFrom: MutableSet<CommandDispatcher<*>> = Collections.newSetFromMap(IdentityHashMap())
    val commandsToRegister = mutableMapOf<String, (List<String>) -> Unit>()

    override fun injectPlatform(di: DependencyInjection) {
        di.registerPlatform<ChatAPI> {
            object : ChatAPI {
                override fun sendToPlayer(text: Text) {
                    messageQueue.add(text)
                }

                override fun registerCommand(label: String, handler: (List<String>) -> Unit) {
                    commandsToUnregister.add(label)
                    commandsToRegister[label] = handler
                    ClientCommandManager.getActiveDispatcher()?.let { tryRegisterAllCommands(it) }
                }
            }
        }
        di.registerPlatform<SchedulingAPI> {
            object : SchedulingAPI {
                override fun callSoon(block: () -> Unit) {
                    scheduledExecutables.add(block)
                }
            }
        }
    }

    fun tryRegisterAllCommands(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        val root = dispatcher.root
        dispatchersToUnregisterFrom.add(dispatcher)
        commandsToRegister.forEach { (label, handler) ->
            if (root.getChild(label) == null) {
                fun n(name: String) =
                    ClientCommandManager.literal(name)
                        .executes {
                            handler(emptyList())
                            0
                        }
                        .then(ClientCommandManager.argument("text", StringArgumentType.greedyString())
                            .executes {
                                handler(StringArgumentType.getString(it, "text").split(" "))
                                0
                            })

                dispatcher.register(n(label))
                dispatcher.register(n("skyneamoe:$label"))
            }

        }
    }

    override fun driveBus(eventBus: EventBus) {
        this.eventBus = eventBus
    }

    override fun invokeLater(function: () -> Unit) {
        MinecraftClient.getInstance().execute(function)
    }

    override fun prepareUnload() {
        scheduledExecutables.clear()
        dispatchersToUnregisterFrom.forEach { dispatcher ->
            val root = dispatcher.root ?: return@forEach
            root as AccessorCommandNode<*>
            commandsToUnregister.forEach {
                root.children_skyneamoe.remove(it)
                root.children_skyneamoe.remove("skyneamoe:$it")
            }
        }
        commandsToRegister.clear()
        dispatchersToUnregisterFrom.clear()
        commandsToUnregister.clear()
    }

    override fun acquiesceUnload() {
    }
}