package moe.nea.sky.platform.forge

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender

class ModuleCommand(val name: String, val handler: (List<String>) -> Unit) : CommandBase() {
    override fun getCommandName(): String {
        return name
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean {
        return true
    }

    override fun getCommandUsage(iCommandSender: ICommandSender?): String {
        return ""
    }

    override fun processCommand(iCommandSender: ICommandSender?, strings: Array<out String>) {
        return handler(strings.toList())
    }
}
